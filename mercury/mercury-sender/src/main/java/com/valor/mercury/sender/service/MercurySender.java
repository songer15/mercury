package com.valor.mercury.sender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valor.mercury.sender.util.Base64Util;
import com.valor.mercury.sender.util.DeflateUtil;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MercurySender {
    private static boolean isInit = false;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, MercurySender> senders = new HashMap<>();
    private static final int maxQueueSizeForClientUse = 5000;
    private static final int maxQueueSizeForServerUse = 200000;
    private static final int defaultQueueSize = 2000;
    private String appId;
    private String appKey;
    private String key;
    private boolean forClientUse;
    private int postBatchSize = 1000;
    private int postBatchInterval = 1000;
    private boolean debug;
    private int triesOnServer;
    private int triesTotal;
    private int maxTriesOnServer = 2;
    private int maxTriesTotal = 6;
    private int currentServer = -1;
    private Queue<Integer> candidateServers;
    private String[] serverUrls;
    private BlockingQueue<MetricMessage> bufferQueue;
    private List<MetricMessage> list;
    private long lastSendTimeMillis;

    public static void init(Map<String, String[]> urls, String appId, String appKey, int postBatchSize, int postBatchInterval, int triesOnServer, int triesTotal, int queueSize, boolean debug, boolean forClientUse) {
        if (isInit) return;
        for (String key : urls.keySet()) {
            senders.put(key, new MercurySender(key, urls.get(key), appId, appKey, postBatchSize, postBatchInterval, triesOnServer, triesTotal, queueSize, debug, forClientUse));
        }
        isInit = true;
    }

    public static void init(Map<String, String[]> urls, String appId, String appKey, boolean forClientUse) {
        if (isInit) return;
        for (String key : urls.keySet()) {
            senders.put(key, new MercurySender(key, urls.get(key), appId, appKey, -1, -1, -1, -1, -1, false, forClientUse));
        }
        isInit = true;
    }

    public static void init(Map<String, String[]> urls, String appId, String appKey, int queueSize, boolean forClientUse) {
        if (isInit) return;
        for (String key : urls.keySet()) {
            senders.put(key, new MercurySender(key, urls.get(key), appId, appKey, -1, -1, -1, -1, queueSize, false, forClientUse));
        }
        isInit = true;
    }


    public static void init(Map<String, String[]> urls, String appId, String appKey, int queueSize, boolean debug, boolean forClientUse) {
        if (isInit) return;
        for (String key : urls.keySet()) {
            senders.put(key, new MercurySender(key, urls.get(key), appId, appKey, -1, -1, -1, -1, queueSize, debug, forClientUse));
        }
        isInit = true;
    }

    private MercurySender(String key, String[] urls, String appId, String appKey, int postBatchSize, int postBatchInterval, int maxTriesOnServer, int maxTriesTotal, int queueSize, boolean debug, boolean forClientUse) {
        this.forClientUse = forClientUse;
        this.debug = debug;
        this.key = key;
        this.appId = appId;
        this.appKey = appKey;
        if (postBatchSize > 0)
            this.postBatchSize = postBatchSize;
        if (postBatchInterval > 0)
            this.postBatchInterval = postBatchInterval;
        if (queueSize > 0) {
            if (forClientUse) {
                this.bufferQueue = new LinkedBlockingQueue<>(Math.min(queueSize, maxQueueSizeForClientUse));
            } else {
                this.bufferQueue = new LinkedBlockingQueue<>(Math.min(queueSize, maxQueueSizeForServerUse));
            }
        } else {
            this.bufferQueue = new LinkedBlockingQueue<>(defaultQueueSize);
        }
        if (maxTriesOnServer > 0)
            this.maxTriesOnServer = maxTriesOnServer;
        if (maxTriesTotal > 0)
            this.maxTriesTotal = maxTriesTotal;
        this.list = new ArrayList<>();
        this.lastSendTimeMillis = System.currentTimeMillis();
        if (urls != null && urls.length > 0) {
            this.serverUrls = urls;
            this.candidateServers = new ArrayBlockingQueue<>(serverUrls.length);
            List<Integer> serverList = new ArrayList<Integer>() {
                {
                    for (int i = 0; i < serverUrls.length ; i++)
                        this.add(i);
                }
            };
            Collections.shuffle(serverList);
            this.candidateServers.addAll(serverList);
        } else
            throw new IllegalArgumentException("server size must be greater than 0");
        nextServer();
        new Thread(this::run, this.key + "-" + "wordThread").start();
    }

    public static void put(String key, String eventName, Map<String, Object> fieldsMap) {
        if (isInit && senders.containsKey(key) && fieldsMap != null && fieldsMap.size() > 0) {
            senders.get(key).put(eventName, fieldsMap);
        }
    }

    public void put(String eventName, Map<String, Object> fieldsMap) {
        if (forClientUse) {
            //为保证效率，客户端在队列满的情况下直接丢
            if(!bufferQueue.offer(new MetricMessage(eventName, fieldsMap)) && debug)
                System.out.println("queue is full");
        } else {
            try {
                //为保证准确性，服务端在队列满的情况下阻塞
                bufferQueue.put(new MetricMessage(eventName, fieldsMap));
            } catch (InterruptedException e) {
                System.err.println(e.getCause().getMessage());
            }
        }
    }

    private void run() {
        while (true) {
            try {
                MetricMessage wrapperEntity = bufferQueue.poll(postBatchInterval, TimeUnit.MILLISECONDS);
                if (wrapperEntity != null)
                    list.add(wrapperEntity);
                if (list.size() >= postBatchSize || (list.size() > 0 && System.currentTimeMillis() - lastSendTimeMillis >= postBatchInterval)) { // 当前list数据量超过sendPackageSize 或 等待时间超过 postBatchInterval
                    sendAndClear();
                }
            } catch (Exception e) {
                if (debug) {
                    System.out.println(key + " : " + "send data error:" + e.getMessage());
                }
            }
        }
    }

    private void sendAndClear() {
        triesTotal = maxTriesTotal;
        triesOnServer = maxTriesOnServer;
        sendWithLimitedTries();
        lastSendTimeMillis = System.currentTimeMillis();
        list.clear();
    }

    private void sendWithLimitedTries() {
        if (triesTotal > 0) {
            if (triesOnServer == 0)
                nextServer();
            if (!send())
                sendWithLimitedTries();
        } else if (triesTotal == 0 && debug) {
            System.out.println(key + " : " + "aborted after "+ maxTriesTotal + " tries");
        } else {
            throw new IllegalStateException(key + " : " + "bad tryTimes:" + " " + "triesOnServer:" + " " +  triesOnServer + ""  +  "triesTotal:"+ " " + triesTotal);
        }
    }

    private void nextServer() {
        //初始化
        if (currentServer == -1) {
            currentServer = candidateServers.remove();
        } else {
            int lastServer = currentServer;
            currentServer = candidateServers.remove();
            candidateServers.offer(lastServer);
            triesOnServer = Math.min(maxTriesOnServer, triesTotal);
            if (debug) {
                System.out.println(key + " : " + "server: [" + serverUrls[lastServer] + "] is not available, switched to  " + serverUrls[currentServer]);
            }
        }
    }

    private boolean send() {
        triesOnServer--;
        triesTotal--;
        HttpURLConnection urlConnection = null;
        String serverUrl = serverUrls[currentServer];
        try {
            URL url = new URL(serverUrl + "/metric/batchpost/v1");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(5 * 1000);
            urlConnection.setConnectTimeout(1000 * 10);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("User-Agent", "Mercury Sender 1.0");
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            // 将数组转成json，进行压缩，再加密处理
            String value = Base64Util.getEncoder().encodeToString(DeflateUtil.compressStr(objectMapper.writeValueAsString(list)));
            Map<String, String> writeMap = new HashMap<>();
//                        System.out.println(value);
            writeMap.put("value", value);
            writeMap.put("appId", appId);
            writeMap.put("appKey", appKey);
            writer.write(getPostDataString(writeMap));
            writer.flush();
            writer.close();
            outputStream.close();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (debug)
                    System.out.println(key + " : " + "send package success, host: " + serverUrl + " size: " + list.size());
                return true;
            } else {
                if (debug)
                    System.out.println(key + " : " + "send package error to host: " + serverUrl + "response code: " + responseCode + " size: " + list.size());
                return false;
            }
        } catch (Exception e) {
            if (debug)
                System.out.println(key + " : " + "send package error to host: " + serverUrl + " exception: "+ e + " size: " + list.size());
            return false;
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.getInputStream().close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    //do nothing
                }
            }
        }
    }

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

}
