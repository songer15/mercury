package com.valor.mercury.task.flink.mfc.view;

import com.valor.mercury.task.flink.util.DateStr;
import com.valor.mercury.task.flink.util.ESClient;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.types.Row;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gavin
 * 2019/9/26 11:24
 */
public class PageElasticSearchSink_P implements ElasticsearchSinkFunction<Row>, Serializable {
    private String index;

    public PageElasticSearchSink_P(String index) {
        super();
        this.index = index;
    }

    private IndexRequest createIndexRequest(Row element) {
        Map<String, Object> json = new HashMap<>();
        json.put("uniqueCount", element.getField(0));
        json.put("actionTime", new Date((long) element.getField(1)));
        json.put("countryCode", element.getField(2));
        json.put("pageNumber", element.getField(3));
        json.put("device", element.getField(4));
        json.put("language", element.getField(5));
        json.put("macSub",element.getField(6));
        json.put("LocalCreateTime", new Date());
        json.put("aggregationType", "PageAction_Unique");

        String pageName = element.getField(3).toString();
        if (!element.getField(4).equals("phone")) {
            int pageNumber = parsePageNumber(pageName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchQuery("pid", pageNumber));
            sourceBuilder.timeout(TimeValue.timeValueMillis(60_000));
            List<Map> result = ESClient.getInstance().queryEsData(sourceBuilder, "mfc_playlist");
            if (result != null && result.size() > 0) {
                String desc = (String) result.get(0).get("description");
                if (Strings.isEmpty(desc))
                    json.put("title", result.get(0).get("title"));
                else
                    json.put("title", desc);
            }
//        } else if (!element.getField(5).equals("en")) {
//            String prefixName = "";
//            if (pageName.startsWith("TV_HOME :") || pageName.startsWith("MOVIE_HOME :") || pageName.startsWith("RECOMMEND_HOME :")) {
//                prefixName = pageName.substring(0, pageName.indexOf(":") + 2);
//                pageName = pageName.substring(pageName.indexOf(":") + 2);
//            }
//            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//            sourceBuilder.query(QueryBuilders.matchQuery("target", pageName));
//            sourceBuilder.timeout(TimeValue.timeValueMillis(60_000));
//            sourceBuilder.sort("last_modify_time", SortOrder.ASC);
//            List<Map> result = ESClient.getInstance().queryEsData(sourceBuilder, "mfc_playlist_mobile");
//            if (result != null && result.size() > 0)
//                json.put("title", prefixName + result.get(0).get("original").toString());
//            else
//                json.put("title", prefixName + pageName);
        } else
            json.put("title", pageName);

        return Requests.indexRequest()
                .index(index + DateStr.getDateStr(DateStr.GET_DATE_STR.MONTH))
                .type("_doc")
                .source(json);
    }

    @Override
    public void process(Row element, RuntimeContext ctx, RequestIndexer indexer) {
        indexer.add(createIndexRequest(element));
    }

    private Integer parsePageNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
