package com.valor.mercury.task.flink.devops.prt;

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
 * 2020/03/10 14:15
 */
public class PrtElasticSearchSink implements ElasticsearchSinkFunction<Row>, Serializable {
    private String index;

    public PrtElasticSearchSink(String index) {
        super();
        this.index = index;
    }

    private IndexRequest createIndexRequest(Row element) {
        Map<String, Object> json = new HashMap<>();
        json.put("key", element.getField(0).toString());
        json.put("actionTime", new Date((long) element.getField(1)));
        json.put("shareRatio", Double.parseDouble(element.getField(2).toString()));
        json.put("lostRatio", Double.parseDouble(element.getField(3).toString()));
        json.put("type", element.getField(4).toString());
        json.put("connCount", Double.parseDouble(element.getField(5).toString()));
        json.put("LocalCreateTime", new Date());
        return Requests.indexRequest()
                .index(index)
                .type("_doc")
                .source(json);
    }

    @Override
    public void process(Row element, RuntimeContext ctx, RequestIndexer indexer) {
        indexer.add(createIndexRequest(element));
    }
}
