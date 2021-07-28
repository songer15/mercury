package com.valor.mercury.task.fink.live.entry

import java.util

import com.valor.mercury.task.fink.live.constant.FlinkConstants
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.flink.util.Collector
import org.elasticsearch.action.update.UpdateRequest

/**
 * 从 oauth_charge_log_tve 和 bill_invoice_tve 这2个采集流里通过id匹配充值记录，得到会员网站充值记录的类型 product_name（tve月卡，tve年卡，tve年+mfc账号），更新回tve_account_charge_log索引
 */

object TveMemberChargeTypeAnalyse extends FlinkConstants {

  def main(env: StreamExecutionEnvironment, windowSizeMinute : Int): Unit = {
//    //默认120分钟执行一次
//    val windowSizeMinute = if(args.length != 0) args(0).toInt else 120
//    val env = getFlinkEnv(Some(2))

    //tve
    val tve_sink = new ElasticsearchSink.Builder[ChargeType](httpHost, new TveMemberChargeTypeSinkFunction("tve_account_charge_log"))
    tve_sink.setBulkFlushMaxActions(1000)
    tve_sink.setBulkFlushInterval(60000)

    //把oauth_charge_log_tve中的 "charge_invoice" 字段 和 member_order_tve 中的 "id" 字段匹配，再取出后者中的 goods_id
    val tveOauthStream = env
      .addSource(getKafkaSource("oauth_charge_log_tve", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("charge_invoice").asText, node.get("custom_charge_id").asText())
      })

    val tveMemberOrderStream = env
      .addSource(getKafkaSource("member_order_tve", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("id").asText, node.get("goods_id").asText())
      })

    tveOauthStream
      .join(tveMemberOrderStream)
      .where(_._1)  // charge_invoice
      .equalTo(_._1) // id
      .window(TumblingProcessingTimeWindows.of(Time.minutes(windowSizeMinute)))
      .apply((v1, v2, out: Collector[ChargeType]) => out.collect(ChargeType(v1._2, v2._2))) // (custom_charge_id, goods_id)
      .addSink(tve_sink.build())




    //tve_es
    val tve_es_sink = new ElasticsearchSink.Builder[ChargeType](httpHost, new TveMemberChargeTypeSinkFunction("account_charge_log_mix"))
    tve_es_sink.setBulkFlushMaxActions(1000)
    tve_es_sink.setBulkFlushInterval(60000)

    val tve_es_oauthStream = env
      .addSource(getKafkaSource("oauth_charge_log_tve_es", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("charge_invoice").asText, node.get("custom_charge_id").asText())
      })

    val tve_es_memberOrderStream = env
      .addSource(getKafkaSource("member_order_tve_es", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("id").asText, node.get("goods_id").asText())
      })

    tve_es_oauthStream
      .join(tve_es_memberOrderStream)
      .where(_._1)  //
      .equalTo(_._1) // id
      .window(TumblingProcessingTimeWindows.of(Time.minutes(windowSizeMinute)))
      .apply((v1, v2, out: Collector[ChargeType]) => out.collect(ChargeType(v1._2, v2._2))) // (custom_charge_id, goods_id)
      .addSink(tve_es_sink.build())


    //red
    val redsink = new ElasticsearchSink.Builder[ChargeType](httpHost, new TveMemberChargeTypeSinkFunction("account_charge_log_redplay_mobile"))
    redsink.setBulkFlushMaxActions(3)
    redsink.setBulkFlushInterval(60000)

    //把tve_oauth_charge_log 中的 "charge_invoice" 字段 和 bill_invoice 中的 "iid" 字段匹配，再取出后者中的 goods_id
    val redoauthStream = env
      .addSource(getKafkaSource("oauth_charge_log_red", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("charge_invoice").asText, node.get("custom_charge_id").asText())
      })

    val redbillInvoiceStream = env
      .addSource(getKafkaSource("bill_invoice_red", "Metric", bootstrapServers).setStartFromLatest())
      .map(value => {
        val node = value.get("value")
        (node.get("id").asText, node.get("goods_id").asText())
      })

    redoauthStream
      .join(redbillInvoiceStream)
      .where(_._1)  // charge_invoice
      .equalTo(_._1) // iid
      .window(TumblingProcessingTimeWindows.of(Time.minutes(windowSizeMinute)))
      .apply((v1, v2, out: Collector[ChargeType]) => out.collect(ChargeType(v1._2, v2._2))) // (custom_charge_id, product_name)
      .addSink(redsink.build())

    //env.execute(this.getClass.getSimpleName)
  }


  class TveMemberChargeTypeSinkFunction(val index: String) extends ElasticsearchSinkFunction[ChargeType] {
    override def process(element: ChargeType, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
      indexer.add(createIndexRequest(element))
    }
    private def createIndexRequest(element: ChargeType) = {
      val json: util.Map[String, Any] = new util.HashMap[String, Any]
      json.put("goods_id", element.goodsId)
      new UpdateRequest(index, "_doc", element.customChargeId).doc(json)
    }
  }

  case class ChargeType(customChargeId: String, goodsId: String)

}
