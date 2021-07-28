//package com.valor.mercury.task.fink.live.entry
//
//import org.apache.commons.lang3.StringUtils
//import org.apache.flink.api.common.functions.AggregateFunction
//import org.apache.flink.api.scala._
//import org.apache.flink.streaming.api.windowing.time.Time
//import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
//import java.util
//
//import com.valor.mercury.task.fink.live.constant.FlinkConstants
//import com.valor.mercury.task.fink.live.function.PostPaidValidUserMessage.{PostPaidValidUserDTO, PostPaidValidUserResult}
//import org.apache.flink.api.common.functions.RuntimeContext
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
//import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
//import org.elasticsearch.action.update.UpdateRequest
//import org.elasticsearch.common.xcontent.XContentType
//
///**
// * 每天统计所有后付费的渠道的有效用户
// */
//
//object PostPaidValidUserAnalyse extends FlinkConstants{
//
//  def main(env: StreamExecutionEnvironment, windowSizeMinute : Int): Unit = {
//
//
//    //ES sink
//    val sink = new ElasticsearchSink.Builder[PostPaidValidUserResult](httpHost, new PostPaidValidUserSinkFunction("post_paid_valid_user"))
//    sink.setBulkFlushMaxActions(3)
//    sink.setBulkFlushInterval(60000)
//
//    env
//      .addSource(getKafkaSource("post_paid_valid_user", "Metric", bootstrapServers).setStartFromLatest())
//      .map(value => {
//        val node = value.get("value")
//         PostPaidValidUserDTO(
//           node.get("status").asInt,
//           node.get("trial").asInt,
//           if(node.get("device_unique_id") != null) Some(node.get("device_unique_id").asText()) else None,
//           node.get("login_method").asInt,
//           node.get("source").asText(),
//           node.get("expire_ts").asLong,
//           if(node.get("release_id").asText == "rpm") "redplaybox" else node.get("release_id").asText )
//      })
//      .keyBy("releaseId", "source")
//      .timeWindow(Time.minutes(windowSizeMinute)) //采集任务每天12点，收集到数据后60分钟内写入ES
//      .aggregate(new CountAggregator)
//      .addSink(sink.build())
//  }
//
//
//  class CountAggregator extends AggregateFunction[PostPaidValidUserDTO, PostPaidValidUserResult, PostPaidValidUserResult] {
//
//    override def createAccumulator(): PostPaidValidUserResult =  PostPaidValidUserResult(0, 0, 0, 0, scala.collection.mutable.Map(), 0, 0, 0, null, "", "")
//
//    override def add(value: PostPaidValidUserDTO, accumulator: PostPaidValidUserResult): PostPaidValidUserResult = {
//      val now = System.currentTimeMillis()
//      if (StringUtils.isEmpty(accumulator.releaseId)) accumulator.releaseId = value.releaseId
//      if (StringUtils.isEmpty(accumulator.source)) accumulator.source = value.source
//        value.source match {
//        case "user_probation" =>
//          if (value.expire_ts >= now) //体验期用户
//            accumulator.probationUser = accumulator.probationUser + 1
//        case "account_service" =>
//          if (value.loginMethod == 3 && value.expire_ts >= now && value.trial == 1) { //普通邮箱用户,未充值
//            accumulator.emailUser = accumulator.emailUser + 1
//          } else if (value.loginMethod == 3 && value.trial == 0) { //普通邮箱用户,已充值
//            if (value.expire_ts > now) { //仍在有效期内
//              accumulator.paidUser = accumulator.paidUser + 1
//            } else if (milliSecond2Day(now - value.expire_ts) > 30) { //已过期30天
//              accumulator.expired30DaysEmailUser = accumulator.expired30DaysEmailUser + 1
//            }
//          } else if (value.loginMethod == 5  && value.trial == 0 && value.status == 1 && value.deviceUniqueId.isDefined) { //使用充值码登录的用户,以duid为主键
//            val duid = value.deviceUniqueId.get
//            val expireTsSeq = accumulator.codeUser.get(duid)
//            //获得同一个duid下不同account的expire_ts,放进Seq集合
//            accumulator.codeUser.put(duid, if(expireTsSeq.isDefined) expireTsSeq.get :+ value.expire_ts else Seq(value.expire_ts))
//          } else if (value.loginMethod == 7 && value.trial == 0) { // 手机注册用户
//            accumulator.validMobileUser = accumulator.validMobileUser + 1
//          }
//      }
//      accumulator
//    }
//
//    override def getResult(accumulator: PostPaidValidUserResult): PostPaidValidUserResult = {
//      for (entry <- accumulator.codeUser) {
//        var maxExpireTs = 0L
//        var hasValidAccount = false
//        for (expireTs <- entry._2) {
//          maxExpireTs = if (maxExpireTs < expireTs) expireTs else maxExpireTs
//          if (System.currentTimeMillis() < maxExpireTs ) hasValidAccount = true
//        }
//        //统计已经过期30天的充值码用户，同duid不同account以expire_ts最晚的为准
//        if(milliSecond2Day(maxExpireTs - System.currentTimeMillis()) < -30) accumulator.expired30DaysCodeUser = accumulator.expired30DaysCodeUser + 1
//        //统计有效期内的充值码用户，同duid不同account至少有一个在有效期内，就算作有效
//        if(hasValidAccount) accumulator.validCodeUser = accumulator.validCodeUser + 1
//      }
//      accumulator
//    }
//
//    override def merge(a: PostPaidValidUserResult, b: PostPaidValidUserResult): PostPaidValidUserResult = {
//      null
//    }
//  }
//
//
//  class PostPaidValidUserSinkFunction(val index: String) extends ElasticsearchSinkFunction[PostPaidValidUserResult]{
//    import java.text.SimpleDateFormat
//    val formatter = new SimpleDateFormat("yyyy-MM-dd")
//
//    override def process(element: PostPaidValidUserResult, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
//      indexer.add(createIndexRequest(element))
//    }
//
//    private def createIndexRequest(element: PostPaidValidUserResult): UpdateRequest = {
//      val json: util.Map[String, Any] = new util.HashMap[String, Any]
//      val date = formatter.format(new util.Date())
//      json.put("date", date)
//      json.put("probationUser", element.probationUser)
//      json.put("emailUser", element.emailUser)
//      json.put("paidUser", element.paidUser)
//      json.put("codeUser", element.validCodeUser)
//      json.put("mobileUser", element.validMobileUser)
//      json.put("expire30DaysEmailUser", element.expired30DaysEmailUser)
//      json.put("expire30DaysCodeUser", element.expired30DaysCodeUser)
//      json.put("releaseId", element.releaseId)
//      json.put("source", element.source)
//      json.put("LocalCreateTime", new java.util.Date())
//      val id = element.releaseId + ":" + element.source + ":" + date
//      val updateRequest = new UpdateRequest(index, "_doc", id)
//      updateRequest.doc(json, XContentType.JSON)
//      updateRequest.docAsUpsert(true)
//      updateRequest
//    }
//  }
//
//  def milliSecond2Day(milliSecond2Day: Long): Long = milliSecond2Day / (1000 * 60 * 60 * 24)
//}
