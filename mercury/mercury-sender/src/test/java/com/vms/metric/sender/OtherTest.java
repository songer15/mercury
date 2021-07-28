package com.vms.metric.sender;

import com.valor.mercury.sender.service.MercurySender;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OtherTest {
    private final static String local = "http://localhost:2086";
    private final static String uota = "http://appmetrices.uota.xyz:2095";
    private final static String metric2095 = "http://metric01.mecury0data.xyz:2095";
    private final static String uat01 = "http://51.81.244.223:2082";
    private final static String uat02 = "http://51.81.244.224:2082";
    private final static String uat03 = "http://51.81.244.225:2082";

    private final static String metric01 = "http://51.79.17.96:2086";
    private final static String metric02 = "http://54.39.50.37:2086";
    private final static String metric03 = "http://metric03.mecury0data.xyz:2086";
    private static String metric02Old = "http://appmetric.te999.site:2095";
    public static String flink01Domain = "http://appfmetric.te100.site:2095";
    public static String flink01Ip = "http://149.56.25.85:2095";
    public static String flink02Ip = "http://149.56.25.177:2095";
    public static String flink03Ip = "http://158.69.119.42:2095";

    private final static String mercury_receiver_01 = "http://51.81.244.223:2088";
    private final static String mercury_receiver_02 = "http://51.81.244.224:2088";
    private final static String mercury_receiver_03 = "http://51.81.244.225:2088";

    private static String flink_metric = "http://51.77.42.9:2095";


    private final static String senderKey = "test";
    private final static String appId = "appId";
    private final static String appKey = "appId";

    private static int postBatchInterval = 1_000;

    @Test
    public void testCDN() throws  Exception{
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{flink_metric});
        MercurySender.init(servers, appId, appKey,  true);
        int i = 0;
        while (i < 1000) {
            Map<String, Object> data = new HashMap<>();
            data.put("env", 0);
            data.put("event_name", "PLAYER_BUFFER_END");
            data.put("server_id", "btv-cdn-01-01");
            data.put("channel_id", "test");
            data.put("play_time", 1000);
            data.put("release_id", "test");
            MercurySender.put(senderKey, "player_metric", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);

    }

    @Test
    public void testZookeeper() throws  Exception{
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{uat01});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (i < 1000) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", i);
            data.put("env", 0);
            data.put("event_name", "PLAYER_BUFFER_END");
            data.put("server_id", "btv-cdn-01-01");
            data.put("channel_id", "test");
            data.put("play_time", 1000);
            data.put("release_id", "test");
            MercurySender.put(senderKey, "test_zookeeper", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);

    }
}
