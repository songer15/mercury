package com.valor.mercury.task.flink.mfc.login;

import com.valor.mercury.task.flink.util.DateStr;
import org.apache.flink.api.common.functions.RuntimeContext;
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
public class LoginUpgradeElasticSearchSink implements ElasticsearchSinkFunction<UpgradeActionModel>, Serializable {
    private String index;

    public LoginUpgradeElasticSearchSink(String index) {
        super();
        this.index = index;
    }

    private IndexRequest createIndexRequest(UpgradeActionModel element) {
        Map<String, Object> json = new HashMap<>();
        json.put("loginType", element.getLoginType());
        json.put("userId", element.getUserId());
        json.put("actionTime", new Date(element.getActionTime()));
        json.put("device", element.getDevice());
        json.put("vendorId", element.getVendorId());
        json.put("appVersion", element.getAppVersion());
        json.put("preAppVersion", element.getPreAppVersion());
        json.put("did", element.getDid());
        json.put("LocalCreateTime", new Date());
        json.put("aggregationType", "UpgradeAction");
        return Requests.indexRequest()
                .index(index + DateStr.getDateStr(DateStr.GET_DATE_STR.MONTH))
                .type("_doc")
                .source(json);
    }

    @Override
    public void process(UpgradeActionModel element, RuntimeContext ctx, RequestIndexer indexer) {
        indexer.add(createIndexRequest(element));
    }
}
