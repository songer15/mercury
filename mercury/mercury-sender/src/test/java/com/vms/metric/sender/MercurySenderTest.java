package com.vms.metric.sender;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valor.mercury.sender.service.MercurySender;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MercurySenderTest {
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

    private final static String mercury_receiver_01 = "http://51.79.17.96:2082";
    private final static String mercury_receiver_02 = "http://51.81.244.224:2082";
    private final static String mercury_receiver_03 = "http://51.81.244.225:2082";

    private final static String senderKey = "test";
    private final static String appId = "appId";
    private final static String appKey = "appId";

    private static int postBatchInterval = 1_000;

    @Test
    public void testOnce() throws InterruptedException {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{"http://stream-metric-elb-1449472775.us-east-1.elb.amazonaws.com:2081"});
//        servers.put(senderKey, new String[]{metric01});
        MercurySender.init(servers, appId, appKey, 10,true, true);
        int i = 0;
        while (i < 2) {
            Map<String, Object> data = getSampleData(0);
            data.put("release_id", "redpro2");
            data.put("player", i);
            data.put("channel_id", "23");
            MercurySender.put(senderKey, "tracker-server_stat", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);
    }

    @Test
    public void testSmallQueueForClientUse() throws InterruptedException {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{uat01, uat02});
        MercurySender.init(servers, appId, appKey, 10,true, true);
        int i = 0;
        while (i < 10000) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "mfc_user_info", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);
    }

    @Test
    public void testDefaultQueueForClientUse() throws InterruptedException {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{"192.168.196.252:9200"});
        MercurySender.init(servers, appId, appKey, -1, true, true);
        int i = 0;
        while (i < 10000) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "mfc_user_info", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);
    }

    @Test
    public void testTries() throws  Exception {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{uat01});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (i < 100) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", 21);
            data.put("reportTime", "2020-10-15");
            MercurySender.put(senderKey, "tve_feedback_info_mix", data);
            i ++;
        }
        Thread.sleep(postBatchInterval * 10);
    }

    @Test
    public void testBadTries() {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{"unknown1", "unknown2", "unknown3", "unknown4"});
        MercurySender.init(servers, appId, appKey, false);
        while (true) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "spider_test1", data);
        }
    }

    @Test
    public void testSpecificTimes() throws Exception{
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (i < 100) {
            Map<String, Object> data = getSampleData(i);
            MercurySender.put(senderKey, "spider_test1", data);
            i++;
            Thread.sleep(100);
        }
        Thread.sleep(postBatchInterval * 10);
    }

    @Test
    public void testThroughOutput() {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01, mercury_receiver_02, mercury_receiver_03});
        MercurySender.init(servers, appId, appKey,false);
        while (true) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "spider_test1", data);
        }
    }

    @Test
    public void testCreateIndex() {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01});
        MercurySender.init(servers, appId, appKey,-1, true, false);
        while (true) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "account_charge_log_bluetv", data);
        }
    }

    @Test
    public void noServersAvailable() throws Exception {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{"123", "321"});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (true) {
            Map<String, Object> data = getSampleData(i);
            MercurySender.put(senderKey, "spider_test1", data);
            i++;
            Thread.sleep(postBatchInterval);
        }
    }

    @Test
    public void oneServerAvailable() throws Exception {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01, "321"});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (true) {
            Map<String, Object> data = getSampleData(i);
            MercurySender.put(senderKey, "spider_test1", data);
            i++;
            Thread.sleep(postBatchInterval);
        }
    }


    @Test
    public void allServerAvailable() throws Exception {
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01, mercury_receiver_02, mercury_receiver_03});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (true) {
            Map<String, Object> data = getSampleData(i);
            MercurySender.put(senderKey, "spider_test1", data);
            i++;
            Thread.sleep(postBatchInterval);
        }
    }

    @Test
    public void testControlChar() throws Exception {
        String json =
                "{\n" +
                        "        \"account_type\":1,\n" +
                        "        \"limit_date\":\"WITHOUT END\",\n" +
                        "        \"last_modify_time\":\"2021-01-01T10:01:57.000Z\",\n" +
                        "        \"purchase_status\":1,\n" +
                        "        \"pid\":1,\n" +
                        "        \"next_payment_date\":\"2021-02-05T16:00:00.000Z\",\n" +
                        "        \"times\":1,\n" +
                        "        \"renewal_date\":\"2020-12-05T16:00:00.000Z\",\n" +
                        "        \"id\":229519,\n" +
                        "        \"payment_source\":\"SHAREIT\",\n" +
                        "        \"CountryCode\":\"RS\",\n" +
                        "        \"origin_iid\":11501075,\n" +
                        "        \"period\":\"MONTHLY\",\n" +
                        "        \"create_time\":\"2020-12-06T17:43:17.000Z\",\n" +
                        "        \"AutonomousSystemNumber\":31042,\n" +
                        "        \"data_ver\":-1,\n" +
                        "        \"LocalCreateTime\":\"2021-01-02T03:19:10.103Z\",\n" +
                        "        \"StateCode\":\"04\",\n" +
                        "        \"CityName\":\"Pančevo\",\n" +
                        "        \"AutonomousSystemOrganization\":\"Serbia BroadBand-Srpske Kablovskemreze d.o.o.\",\n" +
                        "        \"GeoHash\":\"sryxm8mz3g75\",\n" +
                        "        \"account_id\":1917635,\n" +
                        "        \"round\":1,\n" +
                        "        \"create_status\":0,\n" +
                        "        \"LocalCreateTimestamps\":1609557550103,\n" +
                        "        \"order_id\":\"693031663-1\",\n" +
                        "        \"last_modify_ip\":\"89.216.132.214\",\n" +
                        "        \"status\":1,\n" +
                        "        \"Location\":{\n" +
                        "            \"lon\":20.6403,\n" +
                        "            \"lat\":44.8708\n" +
                        "        },\n" +
                        "        \"invoice_expire_date\":\"2021-01-11T16:00:00.000Z\",\n" +
                        "        \"current_iid\":11608392\n" +
                        "    }";
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String, String[]> servers = new HashMap<>();
        servers.put(senderKey, new String[]{mercury_receiver_01, mercury_receiver_02});
        MercurySender.init(servers, appId, appKey, false);
        int i = 0;
        while (i < 2000) {
            Map<String, Object> map  = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            MercurySender.put(senderKey, "control_char_test", map);
            i++;
            Thread.sleep(postBatchInterval);
        }
    }

    @Test
    public void testRedtvStick() throws InterruptedException {
        Map<String, String[]> servers = new HashMap<>();
//        servers.put(senderKey, new String[]{"http://mix.es.inner003.xyz:2086"});
        servers.put(senderKey, new String[]{metric01});

        MercurySender.init(servers, appId, appKey,-1, true, false);
        int i = 0;
        while (i < 1) {
            Map<String, Object> data = getSampleData(0);
            MercurySender.put(senderKey, "RedtvStick", data);
            i++;
            Thread.sleep(postBatchInterval * 5);
        }
    }





    @Ignore
    public Map<String, Object> getSampleData(int i) {
        Map<String, Object> map = new HashMap<>();
        map.put("intValue", i + 1);
        map.put("eventTimestamp", System.currentTimeMillis());
        map.put("doubleValue", 0.0542127542);
        map.put("longValue", System.currentTimeMillis());
        map.put("dateValue", new Date());
        map.put("booleanValue", true);
        if (i % 2 == 0) {
            map.put("booleanValue", true);
            map.put("strValue", "test2Data");
        } else {
            map.put("strValue", "test1Data");
            map.put("booleanValue", false);
        }
        return map;
    }

    public void initSender(String[] urls) {
        Map<String, String[]> map = new HashMap<>();
        map.put(senderKey, urls);
        MercurySender.init(map, appId, appKey, false);
    }




}


