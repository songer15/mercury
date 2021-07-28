package com.valor.mercury.executor;

import com.google.common.collect.ImmutableSet;
import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.executor.flink.FlinkTaskExecutor;
import com.valor.mercury.executor.hive.HiveTaskExecutor;
import com.valor.mercury.executor.script.groovy.ScriptTaskExecutor;

public class ExecutorMain {
    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        TaskExecutor scriptTaskExecutor = new ScriptTaskExecutor();
        scriptTaskExecutor.start();
        TaskExecutor flinkTaskExecutor = new FlinkTaskExecutor();
        flinkTaskExecutor.start();
        TaskExecutor hiveTaskExecutor = new HiveTaskExecutor();
        hiveTaskExecutor.start();
    }
}
