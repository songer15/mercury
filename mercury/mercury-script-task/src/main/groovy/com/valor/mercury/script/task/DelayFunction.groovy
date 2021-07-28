package com.valor.mercury.script.task

import com.valor.mercury.script.MetricAnalyse
import com.valor.mercury.script.util.IncrementValueHandler

/**
 * @author Gavin* 2020/12/22 16:03
 * 在任务流中起到延时作用
 */
class DelayFunction implements MetricAnalyse {

    @Override
    void run(String[] args) throws Exception {
        String config = (args.size() == 1) ? "600000" : args[1] //默认休眠10分钟
        try {
            Thread.sleep(config.toInteger())
        } catch (Exception exception) {
            IncrementValueHandler.setErrorMsg(this.getClass().getName(), exception.toString())
            System.exit(1)
        }
    }
}
