//package com.valor.mercury.executor.test;
//
//import com.mfc.config.ConfigTools3;
//import com.valor.mercury.common.util.PostUtil;
//import com.valor.mercury.executor.TaskExecutor;
//import com.valor.mercury.executor.flink.FlinkTaskExecutor;
//import org.junit.Test;
//
//public class FlinkTaskTest {
//
//
//    @Test
//    public void flink01() {
//        try {
//            ConfigTools3.load("cfg");
//            TaskExecutor taskExecutor = new FlinkTaskExecutor();
//            taskExecutor.start();
//            taskExecutor.run();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void downloadFromMercury() {
//        try {
//            ConfigTools3.load("cfg");
//            PostUtil.downloadFromMercury("5d3f9abbd8a1e83dae7e9778e8cfe515", "C:\\Users\\Administrator\\Downloads\\mercury-flink-task-1.0-SNAPSHOT.jar");
//            PostUtil.downloadFromMercury("120c822de60b26379890e58f562f1455", "mercury-script-task-1.0-SNAPSHOT.jar");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
