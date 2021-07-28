package com.valor.mercury.task.fink.live.function

object GoosePostPaidValidUserMessage {

case class GoosePostPaidValidUserDTO(tableName: String,
                                     isTrial: Int,
                                     isCodeUser: Int,
                                     isBind: Int,
                                     loginMethod: Int,
                                     expire_ts: Long,
                                     productId: String) {
  }

  case class GoosePostPaidValidUserResult(
                                     var probation_user_valid: Int, //用户总数，在体验期
                                     var non_code_user_probation_valid: Int,  // 非充值码账号数，在体验期
                                     var non_code_user_paid_valid: Int,  // 非充值码账号数，已付费，在有效期
                                     var non_code_user_expired_30days: Int,  //非充值码账号数，已付费，过期30天以上
                                     var code_user_valid: Int,  //充值码账号数，在有效期
                                     var email_login: Int, //邮箱用户
                                     var mobile_login: Int, //手机登录用户
                                     var date: java.util.Date, //统计日期
                                     var product_id: String, //product_id
                                     var table_name : String) //
  {}

}

class GoosePostPaidValidUserMessage{

}
