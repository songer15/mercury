package com.valor.mercury.task.fink.live.entry

import java.util

import com.valor.mercury.task.fink.live.constant.FlinkConstants
import com.valor.mercury.task.fink.live.function.GoosePostPaidValidUserMessage
import com.valor.mercury.task.fink.live.function.GoosePostPaidValidUserMessage.{GoosePostPaidValidUserDTO, GoosePostPaidValidUserResult}
import org.apache.flink.api.scala._
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.functions.{AggregateFunction, RuntimeContext}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentType

object GoosePostPaidValidUserAnalyse extends FlinkConstants{
  def main(env: StreamExecutionEnvironment, windowSizeMinute : Int): Unit = {
    //ES sink
    val sink = new ElasticsearchSink.Builder[GoosePostPaidValidUserResult](httpHost, new GoosePostPaidValidUserSinkFunction("goose_post_paid_valid_user"))
    sink.setBulkFlushMaxActions(3)
    sink.setBulkFlushInterval(60000)
    env
      .addSource(getKafkaSource("goose_post_paid_valid_user", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        val properties = node.get("properties")
        GoosePostPaidValidUserMessage.GoosePostPaidValidUserDTO(
          node.get("table_name").asText(),
          //是否试用用户
          if(properties != null)properties.asInt() >> 1 & 1 else -1,
          //是否充值码用户
          if(properties != null)properties.asInt() >> 9 & 1 else -1,
          //是否绑定
          if(properties != null)properties.asInt() >> 11 & 1 else -1,
          if (node.get("login_method") != null) node.get("login_method").asInt() else -1,
          if (node.get("expire_ts") != null) node.get("expire_ts").asLong() else -1,
          if (node.get("product_id") != null) node.get("product_id").asText() else "-1"
        )
      })
      .keyBy("productId", "tableName")
      .timeWindow(Time.minutes(windowSizeMinute))
      .aggregate(new CountAggregator)
      .addSink(sink.build())
  }

  class CountAggregator extends AggregateFunction[GoosePostPaidValidUserDTO, GoosePostPaidValidUserResult, GoosePostPaidValidUserResult] {

    override def createAccumulator(): GoosePostPaidValidUserResult =  GoosePostPaidValidUserResult(0, 0, 0,  0, 0, 0,0,  null, "", "")

    override def add(value: GoosePostPaidValidUserDTO, accumulator: GoosePostPaidValidUserResult): GoosePostPaidValidUserResult = {
      val now = System.currentTimeMillis()
      if (StringUtils.isEmpty(accumulator.product_id)) accumulator.product_id = value.productId
      if (StringUtils.isEmpty(accumulator.table_name)) accumulator.table_name = value.tableName
      value.tableName match {
        case "tve_account_free_trial" =>
          if (value.expire_ts >= now)
            accumulator.probation_user_valid = accumulator.probation_user_valid + 1
        case "tve_account_subscriptions" =>
          if (value.expire_ts >= now ) {
            if (value.loginMethod == 2 && value.isTrial == 0) //goose里 login_method = 2是邮箱用户
              accumulator.email_login = accumulator.email_login + 1
            else if (value.loginMethod == 6 && value.isTrial == 0) //goose里 login_method = 6是手机用户
              accumulator.mobile_login = accumulator.mobile_login + 1

            //是充值码用户，且还未绑定邮箱。绑定过邮箱的用户不算充值码用户
            if (value.isCodeUser == 1 && value.isBind == 0)
              accumulator.code_user_valid = accumulator.code_user_valid + 1
            //非充值码用户
            else if (value.isCodeUser == 0) {
              if (value.isTrial == 1)
                accumulator.non_code_user_probation_valid = accumulator.non_code_user_probation_valid + 1
              else if (value.isTrial == 0)
                accumulator.non_code_user_paid_valid = accumulator.non_code_user_paid_valid + 1
            }
          }
          if (milliSecond2Day(now - value.expire_ts) > 30 && value.isCodeUser == 0)
              accumulator.non_code_user_expired_30days = accumulator.non_code_user_expired_30days + 1
      }
      accumulator
    }

    override def getResult(accumulator: GoosePostPaidValidUserResult): GoosePostPaidValidUserResult = {
      accumulator
    }

    override def merge(a: GoosePostPaidValidUserResult, b: GoosePostPaidValidUserResult): GoosePostPaidValidUserResult = {
      null
    }
  }


  class GoosePostPaidValidUserSinkFunction(val index: String) extends ElasticsearchSinkFunction[GoosePostPaidValidUserResult]{
    import java.text.SimpleDateFormat
    val formatter = new SimpleDateFormat("yyyy-MM-dd")

    override def process(element: GoosePostPaidValidUserResult, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
      indexer.add(createIndexRequest(element))
    }

    private def createIndexRequest(element: GoosePostPaidValidUserResult): UpdateRequest = {
      val json: util.Map[String, Any] = new util.HashMap[String, Any]
      val date = formatter.format(new util.Date())
      json.put("date", date)
      json.put("probation_user_valid", element.probation_user_valid)
      json.put("non_code_user_expired_30days", element.non_code_user_expired_30days)
      json.put("non_code_user_paid_valid", element.non_code_user_paid_valid)
      json.put("non_code_user_probation_valid", element.non_code_user_probation_valid)
      json.put("code_user_valid", element.code_user_valid)
      json.put("email_login", element.email_login)
      json.put("mobile_login", element.mobile_login)
      json.put("product_id", element.product_id)
      json.put("table_name", element.table_name)
      json.put("LocalCreateTime", new java.util.Date())
      val id = element.product_id + ":" + element.table_name + ":" + date
      val updateRequest = new UpdateRequest(index, "_doc", id)
      updateRequest.doc(json, XContentType.JSON)
      updateRequest.docAsUpsert(true)
      updateRequest
    }
  }

  def milliSecond2Day(milliSecond2Day: Long): Long = milliSecond2Day / (1000 * 60 * 60 * 24)

}
