package com.valor.mercury.task.flink.mfc.play;

import com.valor.mercury.task.flink.util.DateStr;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gavin
 * 2019/10/10 11:15
 */
public class PlayElasticSearchSink_P2 implements ElasticsearchSinkFunction<Row>, Serializable {
    private String index;

    public PlayElasticSearchSink_P2(String index) {
        super();
        this.index = index;
    }

    private IndexRequest createIndexRequest(Row element) {
        Map<String, Object> json = new HashMap<>();
        json.put("uniqueCount", element.getField(0));
        json.put("actionTime", new Date((long) element.getField(1)));
        json.put("countryCode", element.getField(2));
        json.put("device", element.getField(3));
        json.put("language", element.getField(4));
        json.put("playTag", element.getField(5));
        json.put("actionDetail", element.getField(6));
        json.put("macSub",element.getField(7));
        json.put("vendorId",element.getField(8));
        json.put("LocalCreateTime", new Date());
        json.put("aggregationType", "PlayAction_Unique2");
        return Requests.indexRequest()
                .index(index + DateStr.getDateStr(DateStr.GET_DATE_STR.MONTH))
                .type("_doc")
                .source(json);
    }

    @Override
    public void process(Row element, RuntimeContext ctx, RequestIndexer indexer) {
        indexer.add(createIndexRequest(element));
    }
}
