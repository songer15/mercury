package com.valor.mercury.task.fink.live.constant

import java.util
import java.util.Properties

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema
import org.apache.http.HttpHost

trait FlinkConstants {
  val bootstrapServers = "172.16.0.210:9092,172.16.0.211:9092,172.16.0.212:9092"
  var esHost = "172.16.0.201,172.16.0.202"
  val groupID = "metric_live"
  val enableAutoCommit : java.lang.Boolean = false
  val autoCommitIntervals : java.lang.Integer = 60000
  val defaultParallelism = 4

  val defaultWindowSizeMinutes = 10

  val httpHost = util.Arrays.asList(new HttpHost("172.16.0.201", 9200, "http"), new HttpHost("172.16.0.202", 9200, "http"))

  def getFlinkEnv(parallelism : Option[Int]): StreamExecutionEnvironment = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    parallelism match {
      case None => env.setParallelism(defaultParallelism)
      case Some(p) => env.setParallelism(p)
    }
    env
  }

  /**
   *
   */
  def getKafkaParam(broker: String, groupId: String) = {
    val pro = new Properties
    pro.put("bootstrap.servers", broker)
    pro.put("group.id", groupId)
    //pro.put("enable.auto.commit", enableAutoCommit) //kafka 0.9+
    //pro.put("auto.commit.interval.ms", autoCommitIntervals)
    pro
  }

  case class KafkaMessage(topic: String, msg: String)
  case class KafkaTopicOffsetMsg(topic: String, offset: Long, msg: String)

  /**
   *
   */
  def getKafkaSource(topic: String, groupdId: String, broker: String) = new FlinkKafkaConsumer(topic, new JSONKeyValueDeserializationSchema(false), getKafkaParam(broker, groupdId))
}
