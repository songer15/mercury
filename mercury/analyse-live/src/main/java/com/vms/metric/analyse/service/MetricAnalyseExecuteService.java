package com.vms.metric.analyse.service;

import com.vms.metric.analyse.config.MetricAnalyseConfig;
import com.vms.metric.analyse.model.Response;
import com.vms.metric.analyse.model.ResponseEntity;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.tool.CustomTool;
import com.vms.metric.analyse.tool.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class MetricAnalyseExecuteService {

    private static Logger logger = LoggerFactory.getLogger(MetricAnalyseExecuteService.class);

    //任务队列
    private PriorityBlockingQueue<WorkItem> queue = new PriorityBlockingQueue<>();
    private BaseDataAnalyse dataAnalyse;

    @PostConstruct
    public void thread() {
        new Thread(() -> {
            WorkItem workItem;
            while (true) {
                dataAnalyse = null;
                workItem = null;
                try {
                    workItem = queue.take();
                    Response response = new Response();
                    response.setKey(MetricAnalyseConfig.getHttpAuthenticationKey());
                    response.setPassword(MetricAnalyseConfig.getHttpAuthenticationPassword());
                    response.setServerId(MetricAnalyseConfig.getLocalServerId());
                    logger.info("executeAnalyseTask run job:{}", workItem.getTaskName());
                    dataAnalyse = (BaseDataAnalyse) SpringUtil.getBean(workItem.getTaskName());
                    //执行任务
                    workItem = dataAnalyse.execute(workItem);
                    response.setStartTime(workItem.getStartTime());
                    response.setEndTime(workItem.getEndTime());
                    response.setPre(workItem.getPre());
                    response.setTer(workItem.getTer());
                    response.setTotalCount(workItem.getTotalCount());
                    response.setSuccessCount(workItem.getSuccessCount());
                    response.setId(workItem.getId());
                    if (workItem.getResult().equals("true")) {
                        response.setStatus(true);
                        logger.info("executeAnalyseTask run job success:{}", workItem.getTaskName());
                    } else {
                        response.setStatus(false);
                        response.setTer(workItem.getPre());
                        response.setErrorMsg(workItem.getErrorMsg());
                        logger.error("executeAnalyseTask fail,taskName:{}", workItem.getTaskName());
                    }
                    sendResponse(response);
                } catch (InterruptedException e) {
                    logger.error("executeAnalyseTask InterruptedException:{}", e);
                } catch (Exception e) {
                    logger.error("executeAnalyseTask error:{}", e);
                }
            }
        }, "workerThread").start();
    }


    private void sendResponse(Response response) {
        while (true) {
            try {
                logger.info("send response:{}", response.toString());
                String str = MetricAnalyseScheduleService.netRequest(MetricAnalyseConfig.getServerUrlResponse(), CustomTool.objectMapper.writeValueAsString(response));
                if (!str.equals("")) {
                    ResponseEntity<?> responseEntity = CustomTool.objectMapper.readValue(str, ResponseEntity.class);
                    if (responseEntity.getCode() == 200)
                        break;
                    else
                        logger.error("send response feedback error:{}", responseEntity.getMsg());
                } else {
                    logger.error("send response error: feedback is null");
                }
                Thread.sleep(1000 * 60);
            } catch (Exception e) {
                logger.error("send response error:{}", e);
            }
        }
    }


    public void addQueue(WorkItem workItem) {
        queue.add(workItem);
    }
}
