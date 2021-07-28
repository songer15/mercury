package com.valor.mercury.task.fink.live.entry

import org.apache.flink.streaming.api.CheckpointingMode

object Main {

  def main(args: Array[String]): Unit = {
    val env = getFlinkEnv(Some(2))
    env.enableCheckpointing(30000, CheckpointingMode.EXACTLY_ONCE)
//    PostPaidValidUserAnalyse.main(env, if (args(0)!= null) Integer.parseInt(args(0)) else 60)
    GoosePostPaidValidUserAnalyse.main(env, if (args(0)!= null) Integer.parseInt(args(0)) else 60)
    TveMemberChargeTypeAnalyse.main(env, if (args(0)!= null) Integer.parseInt(args(0)) else 60)
    env.execute("MainJob");
  }
}
