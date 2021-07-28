package com.valor.mercury.task.flink.mfc.login;

import com.valor.mercury.task.flink.util.DateStr;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple8;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gavin
 * 2019/9/26 11:24
 */
public class LoginElasticSearchSink implements ElasticsearchSinkFunction<Tuple8<Integer, Integer, Date, Long, String, String, String, String>>, Serializable {
    private String index;

    public LoginElasticSearchSink(String index) {
        super();
        this.index = index;
    }

    private IndexRequest createIndexRequest(Tuple8<Integer, Integer, Date, Long, String, String, String, String> element) {
        Map<String, Object> json = new HashMap<>();
        json.put("count", element.getField(0));
        json.put("uniqueCount", element.getField(1));
        json.put("actionTime", element.getField(2));
        json.put("appVersion", element.getField(3));
        json.put("countryCode", element.getField(4));
        json.put("device", element.getField(5));
        json.put("loginType", element.getField(6));
        json.put("macSub", element.getField(7));
        json.put("LocalCreateTime", new Date());
        json.put("aggregationType", "LoginAction");
        return Requests.indexRequest()
                .index(index + DateStr.getDateStr(DateStr.GET_DATE_STR.MONTH))
                .type("_doc")
                .source(json);
    }

    @Override
    public void process(Tuple8<Integer, Integer, Date, Long, String, String, String, String> element, RuntimeContext ctx, RequestIndexer indexer) {
        indexer.add(createIndexRequest(element));
    }
}
