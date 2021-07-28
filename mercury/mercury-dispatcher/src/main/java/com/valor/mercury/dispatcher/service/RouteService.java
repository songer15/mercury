package com.valor.mercury.dispatcher.service;


import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.DateUtils;
import com.valor.mercury.common.util.ProcessFieldTool;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class RouteService {
    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);
    protected Map<String,  BlockingQueue<Map<String, Object>>> queues = new ConcurrentHashMap<>();
    private Map<String, MessageSendResult> messageSendResults = new ConcurrentHashMap<>();
    private Map<String, Long> lastSentTimestamps = new ConcurrentHashMap<>();
    private int subQueueSize = ConfigTools3.getConfigAsInt("message.subqueue.size",200000);
    private int sendBatchSize = ConfigTools3.getConfigAsInt("send.batch.size",1000);
    protected boolean needBatchSend = false;
    private long sendBatchDur = ConfigTools3.getConfigAsLong("send.batch.duration", 1000L * 60);
    protected RouteThread[] routeThreads = null;
    protected Router.Dest dest = null;

    /**
     * 发送消息到指定的存储位置
     */
    abstract public boolean route(List<Map<String, Object>> list, Router router);

    public void startRouteThread() {
        for (int i = 0; i < routeThreads.length; i++) {
            routeThreads[i] = new RouteThread();
            routeThreads[i].setName(dest.value + "SendThread" + i);
            routeThreads[i].start();
        }
        new Thread(this::routeData, dest.value + "RouteThread").start();
        logger.info(this.getClass().getSimpleName() + " initialized.");
    }

    public void beforeRoute(List<Map<String, Object>> list, Router router) {
        ConfigManager.serviceMonitor.addConsumeCount(router.instanceId,  list.size());
    }

    public void afterSuccessfulRoute(List<Map<String, Object>> list, Router router) {
        logger.info("success sent data to [{}] at [{}],  type : [{}], size:[{}], queue size: [{}]",
                dest.value,
                router.destLocation,
                router.type,
                list.size(),
                queues.get(router.type).size());
        lastSentTimestamps.put(router.type, System.currentTimeMillis());
        ConfigManager.serviceMonitor.addSendCount(router.instanceId, list.size());
    }

    public void afterFailedRoute(List<Map<String, Object>> list, Router router) {
        messageSendResults.putIfAbsent(router.type, new MessageSendResult());
        MessageSendResult messageSendResult = messageSendResults.get(router.type);
        int retrySeq = 1;
        //重新发送直到次数耗尽
        while (retrySeq <= messageSendResult.threshold) {
            logger.error("retrying send to [{}] at [{}], type [{}], size [{}], seq = [{}]", dest.value, router.destLocation, router.type, list.size(), retrySeq);
            boolean result = route(list, router);
            retrySeq++;
            if (result) {
                afterSuccessfulRoute(list, router);
                return;
            } else {
                logger.error("failed retry send to [{}] at [{}], type [{}], size [{}], seq = [{}]", dest.value, router.destLocation, router.type, list.size(), retrySeq);
            }
        }
        logger.error("give up on retry send to [{}] at [{}],  type [{}], size [{}], seq = [{}]", dest.value, router.destLocation, router.type, list.size(), retrySeq);
    }

    public void collectData(Map<String, Object> data, Router router) {
        BlockingQueue<Map<String, Object>> queue = queues.get(router.type);
        if (queue == null) {
            queue =  new LinkedBlockingQueue<>(subQueueSize);
            queues.put(router.type,queue);
        }
        try {
            //todo 考虑是否要用阻塞方法，避免丢弃数据
            queue.put(data);
        } catch (InterruptedException ex) {
            logger.error("type: {}, put queue is interrupted", router.type);
        }
    }

    protected void routeData() {
        while (true) {
            try {
                //待处理数据个数，没有数据则线程休眠，避免空跑占用CPU
                for (Map.Entry<String, BlockingQueue<Map<String, Object>>> data : queues.entrySet()) {
                    String type = data.getKey();
                    BlockingQueue<Map<String, Object>> queue = data.getValue();
                    lastSentTimestamps.putIfAbsent(type, System.currentTimeMillis());
                    long lastSentTimestamp = lastSentTimestamps.get(type);
                    //到达发送条数 || 到达发送时间间隔
                    if (queue.size() >= sendBatchSize || System.currentTimeMillis() - lastSentTimestamp >= sendBatchDur) {
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if (needBatchSend)
                            queue.drainTo(dataList, sendBatchSize);
                        else
                            queue.drainTo(dataList);
                        if (dataList.size() > 0) {
                            Router router = ConfigManager.routerMap.get(type).get(dest);
                            assignThread:
                            while (true) {
                                for (RouteThread routeThread : routeThreads) {
                                    //寻找空闲的线程并提交数据
                                    if (routeThread.prepareRoute(dataList, router)) {
                                        break assignThread;
                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(10);
            } catch (Exception e) {
                ConfigManager.serviceMonitor.saveException(2, "ThreadPool Error: " + DateUtils.getCurrentDateStr(), e);
            }
        }
    }

    public class RouteThread extends Thread {
        Router router;
        List<Map<String, Object>> rawData = new ArrayList<>();
        //true 没有数据在发送中，可以插入数据 false 有数据在发送中，或待发送状态。需要在线程安全的环境下修改此变量
        boolean isIdle = true;
        final Lock lock = new ReentrantLock();
        @Override
        public void run() {
            while (true) {
                if (isReady() && lock.tryLock()) {
                    try {
                        List<Map<String, Object>> list = ProcessFieldTool.processFields(rawData, router);
                        if (list.size() > 0) {
                            beforeRoute(list, router);
                            if (route(list, router))
                                afterSuccessfulRoute(list, router);
                            else
                                afterFailedRoute(list, router);
                        }
                    } catch (Exception ex) {
                        logger.error("Rout data Error, {}", StringTools.buildErrorMessage(ex));
                        ConfigManager.serviceMonitor.saveException(2, "Route data Error: " + DateUtils.getCurrentDateStr(), ex);
                    } finally {
                        //清空数据和router
                        rawData.clear();
                        router = null;
                        isIdle = true;
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }

        public boolean prepareRoute(List<Map<String, Object>> data, Router router) {
            if (!isIdle)
                return false;
            boolean isSuccess = false;
            if (lock.tryLock()) {
                try {
                    this.rawData = data;
                    this.router = router;
                    isSuccess = true;
                } finally {
                    isIdle = false;
                    lock.unlock();
                }
            }
            return isSuccess;
        }

        /**
         * 有数据可发送且router不为null时，return true;
         */
        public boolean isReady() {
            return !rawData.isEmpty() && router != null;
        }
    }




    /**
     * 根据各type发送结果，尝试重发或放回queue里
     */
    private static class MessageSendResult {
        final long millisToReset =  60 * 1000L;
        final int threshold = 3;
        AtomicLong lastRetryTime = new AtomicLong(0);
        AtomicInteger retryTimes = new AtomicInteger(0);

        //如果一段时间内没有发送失败，尝试重置重试次数
        public void reset() {
            if (System.currentTimeMillis() - lastRetryTime.get() > millisToReset && lastRetryTime.get() > 0)
                retryTimes.set(0);
        }

        public boolean isOverRetry() {
            return retryTimes.get() >= threshold;
        }

        public void retryOnce() {
            lastRetryTime.set(System.currentTimeMillis());
            retryTimes.getAndIncrement();
        }
    }

//    /**
//     * message发送控制
//     */
//    private static class MessageSendPlan extends AbstractPrintable {
//        long upperBound = CommonConfig.getSendBatchDuration();
//        long lowerBound = CommonConfig.getSendBatchDuration() / 60;
//        long fixedDuration  = CommonConfig.getSendBatchDuration();
//        int fixedSize  = CommonConfig.getSendBatchSize();
//        //上次批量发送的时间戳
//        AtomicLong lastSentTimestamp = new AtomicLong(System.currentTimeMillis());
//        //上次批量发送的数据条数
//        AtomicInteger lastSentAmount = new AtomicInteger(0);
//        //下次发送的间隔
//        AtomicLong sendDuration = new AtomicLong(CommonConfig.getSendBatchDuration());
//
//        AtomicInteger nextSentAmount = new AtomicInteger(0);
//        /**
//         * 改变下次的发送间隔
//         */
//        public void changeNextSendDuration(double ratio) {
//            long nextSendDuration = (long) (sendDuration.longValue() * ratio);
//            if (nextSendDuration >= lowerBound && nextSendDuration <= upperBound)
//                sendDuration.set(nextSendDuration);
//        }
//
//        public boolean readyToSend () {
//            return readyToSendOnDuration() || readyToSendOnSize();
//        }
//        public boolean readyToSendOnDuration () {
//            return System.currentTimeMillis() - lastSentTimestamp.get() >= fixedDuration;
//        }
//        public boolean readyToSendOnSize () {
//            return nextSentAmount.get() >= fixedSize;
//        }
//    }
}
