//package com.valor.mercury.dispatcher.service;
//
//import com.mfc.config.ConfigTools3;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.apache.curator.framework.recipes.locks.InterProcessMutex;
//import org.apache.curator.retry.ExponentialBackoffRetry;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.TimeUnit;
//
//
//
//public class LockService {
//    private static final Logger logger = LoggerFactory.getLogger(LockService.class);
//    private CuratorFramework client;
//
//    public  LockService(){
//        client = CuratorFrameworkFactory.newClient(ConfigTools3.getConfigAsString("zookeeper.host"), new ExponentialBackoffRetry(1000, 3));
//        client.start();
//    }
//
//    public boolean lock(String key, long time, TimeUnit unit) {
//        try {
//            InterProcessMutex mutex = new InterProcessMutex(client, key);
//            return mutex.acquire(time, unit);
//        } catch (Exception e) {
//            logger.error("get lock error", e);
//            return false;
//        }
//    }
//
//    public boolean releaseLock(String key) {
//        try {
//            InterProcessMutex mutex = new InterProcessMutex(client, key);
//            mutex.release();
//            return true;
//        } catch (Exception e) {
//            logger.error("release lock error", e);
//            return false;
//        }
//    }
//}
