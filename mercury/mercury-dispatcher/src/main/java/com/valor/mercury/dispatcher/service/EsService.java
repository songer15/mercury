package com.valor.mercury.dispatcher.service;
import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.EsClient;
import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.Router;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EsService extends RouteService {
    private static final Logger logger = LoggerFactory.getLogger(EsService.class);
    private static final String TYPENAME = "_doc";
    private EsClient esClient;
    private Map<String, String> esIndexMap = new ConcurrentHashMap<>(); //  indexName -> mappings字符串

    public EsService() {
        dest = Router.Dest.ES;
        needBatchSend = true;
        routeThreads = new RouteThread[ConfigTools3.getConfigAsInt("thread.es.size",2)];
        esClient = new EsClient(ConfigTools3.getAsList("elasticsearch.url", ","));
        super.startRouteThread();
    }

    @Override
    public boolean route(List<Map<String, Object>> data, Router router) {
        try {
            String indexNameToSend = router.currentIndexName;
            createIndexIfAbsent(router, indexNameToSend);
            BulkResponse bulkResponse = esClient.bulkWrite(data, indexNameToSend, TYPENAME, router.idField);
            int success = 0;
            for (BulkItemResponse itemResponse : bulkResponse)
                if (!itemResponse.isFailed())
                    success ++;
            if (bulkResponse.hasFailures()) {
                logger.error("Elasticsearch partial failures: [{}]", bulkResponse.buildFailureMessage());
                logger.error("Elasticsearch partial failures: expected [{}], success [{}]", data.size(), success);
                return false;
            }
            return true;
        } catch (Exception e) {
            ServiceMonitor.getDefault().saveException(1, String.format("type: %s route error", router.type), e);
            logger.error("Send to ES error", e);
            return false;
        }
    }

    //创建索引和Mappings
    public void createIndexIfAbsent(Router router, String indexNameToCreate) {
        if (StringUtils.isEmpty(indexNameToCreate))
            return;
        //todo 在索引被手动删除后， 如果再有数据进来，索引会被自动创建，跳过mapping流程
        if (!esIndexMap.containsKey(indexNameToCreate)) {
            try {
                if (esClient.createIndex(indexNameToCreate, TYPENAME, router.shardNum, router.replicaNum, router.indexMapping))
                    logger.info("[{}}]Created index: {}", this.getClass().getSimpleName(), indexNameToCreate);
                if (esClient.indexExists(indexNameToCreate))
                    esIndexMap.put(indexNameToCreate, router.indexMapping);
            } catch (Exception ex) {
                logger.error("error occurred when creating index", ex);
                logger.error("the router is: [{}]", router.toString());
            }
        }
    }
}
