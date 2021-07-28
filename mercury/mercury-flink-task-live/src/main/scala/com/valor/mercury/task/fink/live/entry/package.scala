package com.valor.mercury.task.fink.live

import com.valor.mercury.task.fink.live.constant.FlinkConstants


package object entry extends FlinkConstants{

  /**
    *
    */
//  def getFlinkEnv(parallelism : Option[Int]): StreamExecutionEnvironment = {
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    parallelism match {
//      case None => env.setParallelism(defaultParallelism)
//      case Some(p) => env.setParallelism(p)
//    }
//    env
//  }

//  /**
//    *
//    */
//  def getKafkaParam(broker: String, groupId: String) = {
//    val pro = new Properties
//    pro.put("bootstrap.servers", broker)
//    pro.put("group.id", groupId)
//    //pro.put("enable.auto.commit", enableAutoCommit) //kafka 0.9+
//    //pro.put("auto.commit.interval.ms", autoCommitIntervals)
//    pro
//  }
//
//  case class KafkaMessage(topic: String, msg: String)
//  case class KafkaTopicOffsetMsg(topic: String, offset: Long, msg: String)
//
//  /**
//    *
//    */
//  def getKafkaSource(topic: String, groupdId: String, broker: String) = new FlinkKafkaConsumer(topic, new JSONKeyValueDeserializationSchema(false), getKafkaParam(broker, groupdId))
}

