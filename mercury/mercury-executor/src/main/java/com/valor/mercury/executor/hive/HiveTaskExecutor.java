package com.valor.mercury.executor.hive;

import com.alibaba.fastjson.JSON;
import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.HiveClient;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.executor.TaskExecutor;
import com.valor.mercury.sender.service.MercurySender;
import java.util.*;
import static com.valor.mercury.common.constant.MercuryConstants.*;

public class HiveTaskExecutor extends TaskExecutor {
    private HiveClient hiveClient;

    public HiveTaskExecutor() {
        List<String> urlList = ConfigTools3.getAsList("mercury.receiver.url");
        Map<String, String[]> urls = new HashMap<>();
        urls.put("hive", urlList.toArray(new String[urlList.size()]));
        MercurySender.init(urls, "hive", "hive", 200000, false);
        hiveClient = new HiveClient(ConfigTools3.getString("hive.hadoop.dfs.username"), ConfigTools3.getConfigAsString("hive.url"));
        executorStatus = EXECUTOR_STATUS_WORKING;
        continuouslyReport = false;
        //hive 任务通常都需要执行较长时间
        concurrentlyRun = true;
    }

    @Override
    public void runTask(ExecutorCommand command, ExecutorReport report) throws Exception {
        HiveTask task = JSON.parseObject(JSON.toJSONString(command.getTaskConfig()), HiveTask.class);
        task.setClientVariables(clientName, clientPsd);
        task.setExecutorVariables(command, report);
        task.client = hiveClient;
        task.init();
        task.exec();
    }

    @Override
    public void continuouslyReport() {

    }

}
