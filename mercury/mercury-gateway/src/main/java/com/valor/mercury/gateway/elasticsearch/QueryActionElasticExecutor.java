package com.valor.mercury.gateway.elasticsearch;

import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;
import com.valor.mercury.gateway.elasticsearch.query.*;
import com.valor.mercury.gateway.elasticsearch.query.join.ESJoinQueryAction;
import com.valor.mercury.gateway.elasticsearch.query.multi.MultiQueryAction;
import com.valor.mercury.gateway.elasticsearch.query.multi.MultiQueryRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;

import java.io.IOException;


/**
 *
 */
public class QueryActionElasticExecutor {
//    public static Object executeSearchActions(DefaultQueryAction searchQueryAction) throws SqlParseException {
//        int scrollSize = 10000;
//        SearchHits firstHits = null;
//        String filePath = "";
//        int searchTimes = 0;
//        int hitsCount = 0;
//        while(true) {
//            //查询
//            SearchResponse searchResponse = (SearchResponse) searchQueryAction.explain().get();
//            SearchHits hits = searchResponse.getHits();
//            searchTimes++;
//            //记录第一次查询的hits
//            if (firstHits == null) {
//                firstHits = hits;
//            }
//            //本次查询到数据
//            if (hits.getTotalHits() > 0) {
//                hitsCount += hits.getTotalHits();
//                //非第一次查询，需要hits保存到文件
//                if (searchTimes > 1) {
//                    //第二次查询要先保存第一次的hits到文件
//                    if (searchTimes == 2) {
//                        saveHits(firstHits, filePath);
//                    }
//                    saveHits(hits, filePath);
//                }
//            } else {
//                //未查询到数据跳出
//              break;
//            }
//            //更新下次的scroll_id
//            searchQueryAction.getForceScroll().setScrollId(searchResponse.getScrollId());
//        }
//        if (hitsCount > scrollSize) {
//            return filePath;
//        } else {
//            return firstHits;
//        }
//    }

    public static SearchHits executeSearchAction(DefaultQueryAction searchQueryAction) throws SqlParseException {
        SearchResponse searchResponse = (SearchResponse) searchQueryAction.explain().get();
        //设置下次查询的scroll_id
        searchQueryAction.getForceScroll().setScrollId(searchResponse.getScrollId());
        //设置之后的查询为非首次查询
        if (searchQueryAction.getForceScroll().isFirstSearch()) searchQueryAction.getForceScroll().setFirstSearch(false);
        return searchResponse.getHits();
    }



    public static SearchHits executeJoinSearchAction(Client client , ESJoinQueryAction joinQueryAction) throws IOException, SqlParseException {
        SqlElasticRequestBuilder joinRequestBuilder = joinQueryAction.explain();
        ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client,joinRequestBuilder);
        executor.run();
        return executor.getHits();
    }

    public static Aggregations executeAggregationAction(AggregationQueryAction aggregationQueryAction) throws SqlParseException {
        SqlElasticSearchRequestBuilder select =  aggregationQueryAction.explain();
        return ((SearchResponse)select.get()).getAggregations();
    }

    public static ActionResponse executeDeleteAction(DeleteQueryAction deleteQueryAction) throws SqlParseException {
        return deleteQueryAction.explain().get();
    }

    public static SearchHits executeMultiQueryAction(Client client, MultiQueryAction queryAction) throws SqlParseException, IOException {
        SqlElasticRequestBuilder multiRequestBuilder = queryAction.explain();
        ElasticHitsExecutor executor = MultiRequestExecutorFactory.createExecutor(client, (MultiQueryRequestBuilder) multiRequestBuilder);
        executor.run();
        return executor.getHits();
    }



    public static Object executeAnyAction(Client client , QueryAction queryAction) throws SqlParseException, IOException {
        if(queryAction instanceof DefaultQueryAction)
            return executeSearchAction((DefaultQueryAction) queryAction);
        if(queryAction instanceof AggregationQueryAction)
            return executeAggregationAction((AggregationQueryAction) queryAction);
        if(queryAction instanceof ESJoinQueryAction)
            return executeJoinSearchAction(client, (ESJoinQueryAction) queryAction);
        if(queryAction instanceof MultiQueryAction)
            return executeMultiQueryAction(client, (MultiQueryAction) queryAction);
        if(queryAction instanceof DeleteQueryAction )
            return executeDeleteAction((DeleteQueryAction) queryAction);
        return null;
    }

}
