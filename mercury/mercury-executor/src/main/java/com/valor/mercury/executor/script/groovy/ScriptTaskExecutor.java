package com.valor.mercury.executor.script.groovy;

import com.alibaba.fastjson.JSON;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.executor.TaskExecutor;
import com.valor.mercury.executor.hive.HiveTask;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.valor.mercury.common.constant.MercuryConstants.*;

public class ScriptTaskExecutor extends TaskExecutor {
    public static Map<Long, GroovyScriptTask> scriptTaskMap = new ConcurrentHashMap<>();
    public static Map<Long, Long> processMap = new ConcurrentHashMap<>();
    private GroovyScriptManager groovyScriptManager = new GroovyScriptManager();

    public ScriptTaskExecutor() {
        executorStatus = EXECUTOR_STATUS_WAITING;
        continuouslyReport = true;
        concurrentlyRun = true;
    }

    @Override
    public void runTask(ExecutorCommand command, ExecutorReport report) throws Exception{
        GroovyScriptTask task = JSON.parseObject(JSON.toJSONString(command.getTaskConfig()), GroovyScriptTask.class);
        scriptTaskMap.put(command.getInstanceID(), task);
        task.setClientVariables(clientName, clientPsd);
        task.setExecutorVariables(command, report);
        task.groovyScriptManager = groovyScriptManager;
        task.init();
        task.exec();
    }

    @Override
    public void continuouslyReport() {
        Iterator<Map.Entry<Long, GroovyScriptTask>> iterator =  scriptTaskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, GroovyScriptTask> entry = iterator.next();
            GroovyScriptTask job = entry.getValue();
            if (EXECUTOR_INSTANCE_STATUS_FAIL.equals(job.executorReport.getInstanceStatus()) || EXECUTOR_INSTANCE_STATUS_SUCCESS.equals(job.executorReport.getInstanceStatus()))
                iterator.remove();
            PostUtil.reportToMercury(job.executorReport);
        }
    }

//    class NoSystemExitSandbox extends GroovyInterceptor {
//        @Override
//        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
//            if (receiver==System.class && method.equals("exit"))
//                throw new SecurityException("No call on System.exit() please");
//            return super.onStaticCall(invoker, receiver, method, args);
//        }
//    }
//
//    class NoRunTimeSandbox extends GroovyInterceptor {
//        @Override
//        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
//            if (receiver==Runtime.class)
//                throw new SecurityException("No call on RunTime please");
//            return super.onStaticCall(invoker, receiver, method, args);
//        }
//    }
//
//    class NoMysqlDatasourceSandbox extends GroovyInterceptor {
//        @Override
//        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
//            if (receiver== DataSource.class)
//                throw new SecurityException("No call on Datasource please");
//            return super.onStaticCall(invoker, receiver, method, args);
//        }
//    }

}
