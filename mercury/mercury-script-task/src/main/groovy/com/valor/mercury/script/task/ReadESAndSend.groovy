package com.valor.mercury.script.task

import com.google.common.collect.Lists
import com.valor.mercury.common.client.EsClient
import com.valor.mercury.script.MetricAnalyse
import org.elasticsearch.common.collect.Tuple
import org.elasticsearch.search.builder.SearchSourceBuilder

class ReadESAndSend implements MetricAnalyse{

    @Override
    void run(String[] args) throws Exception {
        //发送的type
        final String type = "tve_app_device_info_export"
        //读取的index
        final String index = "tve_app_device_info"
        EsClient esClient = new EsClient(Lists.newArrayList("http://metric01.mecury0data.xyz:9201"));

        Map<String, String[]> urls = new HashMap<>();
        urls.put("remote", ["http://metric01.mecury0data.xyz:2082"] as String[]);

        MercurySender.init(urls, "ESdata", "ESdata", 2000, 100, 2, 5, 200000, true, false);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
        searchSourceBuilder.sort("LocalCreateTime")
        searchSourceBuilder.size(2000)
        String scrollId = null
        while (true) {
            try {
                Tuple<String, List<Map<String, Object>>> tuple = esClient.searchByScrollId([index] as String[], ["_doc"] as String[], searchSourceBuilder,  scrollId);
                scrollId = tuple.v1();
                List<Map<String, Object>> data = tuple.v2();
                if (data.isEmpty() || data.size() == 0)
                    break;
                for (Map<String, Object>  e: data) {
                    MercurySender.put("remote", type, e);
                }
                data.clear()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        Thread.sleep(10000);

    }

}
