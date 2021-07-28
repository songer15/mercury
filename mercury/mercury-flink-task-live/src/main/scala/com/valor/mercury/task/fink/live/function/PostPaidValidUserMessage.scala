package com.valor.mercury.task.fink.live.function

object PostPaidValidUserMessage {

  case class PostPaidValidUserDTO(
                                 status: Int,
                                 trial: Int,
                                 deviceUniqueId: Option[String],
                                 loginMethod: Int,
                                 source: String,
                                 expire_ts: Long,
                                 releaseId: String) {
  }

  case class PostPaidValidUserResult(var probationUser: Int, //体验期用户数
                                     var emailUser: Int,  //注册邮箱账号数
                                     var paidUser: Int,  //充值过的账号数
                                     var validMobileUser: Int,
                                     var codeUser: scala.collection.mutable.Map[String, Seq[Long]], //充值码账号集合
                                     var validCodeUser: Int,  //充值码账号数
                                     var expired30DaysEmailUser: Int,  //过期30天以上的邮箱账号数
                                     var expired30DaysCodeUser: Int,  //过期30天以上的充值码duid数
                                     var date: java.util.Date,
                                     var releaseId: String,
                                     var source: String) {
  }

}


class PostPaidValidUserMessage{

}
