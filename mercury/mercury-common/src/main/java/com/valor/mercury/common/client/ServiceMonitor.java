package com.valor.mercury.common.client;


import com.valor.mercury.common.model.AbstractLMI;
import com.valor.mercury.common.model.AbstractPrintable;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.common.util.PropertyUtil;
import com.valor.mercury.common.util.StringTools;
import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServiceMonitor {
    private static ServiceMonitor defaultMonitor = new ServiceMonitor();
    private static final Logger logger = LoggerFactory.getLogger(ServiceMonitor.class);
    private Map<Long, MessageCounter> counts = new ConcurrentHashMap<>();
    private Map<String, RuntimeError> errors = new ConcurrentHashMap<>();

    public static ServiceMonitor getDefault() {
        return defaultMonitor;
    }

    public void addConsumeCount(long instanceId, long num) {
        counts.putIfAbsent(instanceId, new MessageCounter());
        counts.get(instanceId).consumed.getAndAdd(num);
    }

    public void addReceiveCount(long instanceId, long num) {
        counts.putIfAbsent(instanceId, new MessageCounter());
        counts.get(instanceId).received.getAndAdd(num);
    }

    public void addSendCount(long instanceId,  long num) {
        counts.putIfAbsent(instanceId, new MessageCounter());
        counts.get(instanceId).sent.getAndAdd(num);
    }

    public void addForwardCount(long instanceId,  long num) {
        counts.putIfAbsent(instanceId, new MessageCounter());
        counts.get(instanceId).forwarded.getAndAdd(num);
    }

    public void addInterceptedCount(long instanceId,  long num) {
        counts.putIfAbsent(instanceId, new MessageCounter());
        counts.get(instanceId).intercepted.getAndAdd(num);
    }

    public void failedToSend(String type, Throwable e) {
        saveException(2, String.format("Write to %s Error", type), e);
    }

    public void failedToSend(String type, String msg) {
        saveException(2, String.format("Write to %s Error", type), msg);
    }

    public void saveException(int priority, String exceptionId, Object object) {
        if (priority >= 0 && !errors.containsKey(exceptionId) ) {
            errors.put(exceptionId, new RuntimeError(exceptionId, StringTools.buildErrorMessage(object), priority, null));
        }
    }

    public void saveException(long instanceId, Object object) {
        errors.put(String.valueOf(instanceId), new RuntimeError(String.valueOf(instanceId), StringTools.buildErrorMessage(object), null, instanceId));
    }

    public void reportCount(String service) {
        Iterator<Map.Entry<Long, MessageCounter>> iterator = counts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, MessageCounter> entry = iterator.next();
            long instanceId = entry.getKey();
            MessageCounter counter = entry.getValue();
            //移除已经不更新的计数器
            if (counter.isClear()) {
                iterator.remove();
                continue;
            }
            ExecutorReport report = new ExecutorReport(PostUtil.getClientName(), PostUtil.getClientPassword(), instanceId);
            Map<String, Object> metric = new HashMap<>();
            if ("receiver".equals(service)) {
                metric.put("receiveNum", counter.received.getAndSet(0));
                metric.put("forwardNum", counter.forwarded.getAndSet(0));
            } else if ("dispatcher".equals(service)) {
                metric.put("consumeNum", counter.consumed.getAndSet(0));
                metric.put("sendNum", counter.sent.getAndSet(0));
                metric.put("interceptNum", counter.intercepted.getAndSet(0));
            } else if ("spider".equals(service)) {
                metric.put("receiverNum", counter.received.getAndSet(0));
                metric.put("writeNum", counter.sent.getAndSet(0));
            }
            report.setMetrics(metric);
            PostUtil.reportToMercury(report);
        }
    }

    public void reportErrors() {
        Iterator<Map.Entry<String, RuntimeError>> iterator = errors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RuntimeError> entry = iterator.next();
            RuntimeError error = entry.getValue();
            ExecutorReport report = new ExecutorReport(PostUtil.getClientName(), PostUtil.getClientPassword(), error.instanceId);
            report.setErrorMessage(error.errorMessage);
            PostUtil.reportToMercury(report);
            iterator.remove();
            logger.error("Runtime Error: {}", error.toString());
        }
    }

    static class RuntimeError extends AbstractLMI {
        String errorId;
        String errorMessage;
        Integer priority;
        Long instanceId;

        public RuntimeError(String errorId, String errorMessage, Integer priority, Long instanceId) {
            this.errorId = errorId;
            this.errorMessage = errorMessage;
            this.priority = priority;
            this.instanceId = instanceId;
        }
    }

    static class MessageCounter extends AbstractPrintable {
        AtomicLong controllerCalls = new AtomicLong(0);
        AtomicLong forwarded = new AtomicLong(0);
        AtomicLong received = new AtomicLong(0);
        AtomicLong consumed = new AtomicLong(0);
        AtomicLong sent = new AtomicLong(0);
        AtomicLong intercepted = new AtomicLong(0);

        public boolean isClear() {
            return controllerCalls.get() + forwarded.get() + received.get() + consumed.get() + intercepted.get() == 0;
        }
//        AtomicLong es = new AtomicLong(0);
//        AtomicLong kafka = new AtomicLong(0);
//        AtomicLong influxDB = new AtomicLong(0);
//        AtomicLong hdfs = new AtomicLong(0);


//        public void addSendCount(Router.Dest dest, long num) {
//            switch (dest) {
//                case ES: {
//                    es.getAndAdd(num);
//                    break;
//                }
//                case KAFKA:{
//                    kafka.getAndAdd(num);
//                    break;
//                }
//                case INFLUXDB: {
//                    influxDB.getAndAdd(num);
//                    break;
//                }
//                case HDFS: {
//                    hdfs.getAndAdd(num);
//                    break;
//                }
//            }
//        }

    }
}
