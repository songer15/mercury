package com.valor.mercury.gateway.service;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.EsClient;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.gateway.model.dto.goose.GooseQueryResponseDTO;
import com.valor.mercury.gateway.model.query.QueryResult;
import com.valor.mercury.gateway.model.query.SQLQueryResult;
import com.valor.mercury.gateway.model.query.goose.GooseQuery;
import com.valor.mercury.gateway.elasticsearch.QueryActionElasticExecutor;
import com.valor.mercury.gateway.model.dto.goose.GooseQueryRequestDTO;
import com.valor.mercury.gateway.model.dto.QueryRequestDTO;
import com.valor.mercury.gateway.util.FileUtils;
import com.valor.mercury.gateway.model.Row;
import com.valor.mercury.gateway.util.ValidationUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.set.Sets;
import com.valor.mercury.gateway.elasticsearch.executors.CSVResult;
import com.valor.mercury.gateway.elasticsearch.executors.CSVResultsExtractor;
import com.valor.mercury.gateway.elasticsearch.SearchDao;
import com.valor.mercury.gateway.elasticsearch.jdbc.ObjectResult;
import com.valor.mercury.gateway.elasticsearch.jdbc.ObjectResultsExtractor;
import com.valor.mercury.gateway.elasticsearch.query.DefaultQueryAction;
import com.valor.mercury.gateway.elasticsearch.query.QueryAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ElasticSearchQueryService {
    @Autowired
    private FileService fileService;
    private EsClient esClient;
    private final static Logger logger = LoggerFactory.getLogger(ElasticSearchQueryService.class);
    private ObjectResultsExtractor objectResultsExtractor = new ObjectResultsExtractor(false, false, false);
    private CSVResultsExtractor csvResultsExtractor = new CSVResultsExtractor(false, false, false);
    private SearchDao searchDao;

    public ElasticSearchQueryService() {
        esClient = new EsClient(ConfigTools3.getAsList("elasticsearch.url", ","));
//        Settings settings = Settings.builder()
//                .put("client.transport.ignore_cluster_name", true)
//                .put("client.transport.sniff", false)
////                .put("cluster.name", "my-application")
//                .build();
//        // ???????????????
//        TransportClient transportClient = new PreBuiltTransportClient(settings);
//        List<String> esHosts = ConfigTools3.getAsList("elasticsearch.host", ",");
//        for (String esHost : esHosts) {
//            try {
//                transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(esHost), 9300));
//            } catch (UnknownHostException e) {
//                throw new ElasticsearchException(e);
//            }
//        }
//        searchDao = new SearchDao(transportClient);
    }

    //???goose???????????????
    public GooseQueryResponseDTO queryForGoose(GooseQueryRequestDTO dto) {
        GooseQueryResponseDTO gooseQueryResponseDTO = new GooseQueryResponseDTO();
        try {
            //????????????
            ValidationUtils.validate(dto);
            GooseQuery gooseQuery = GooseQuery.getByQueryId(dto.getQueryId());
            //????????????????????????????????????????????????
            gooseQuery.setTimeRange(dto.getStart(), dto.getEnd());
            //????????????productId ??????term??????????????????????????????????????????product
            if (!StringUtils.isEmpty(dto.getProductId()))
                gooseQuery.addTermQuery("productId", dto.getProductId());
            gooseQueryResponseDTO.addResult(esClient.search(gooseQuery.getIndex(), gooseQuery.getType(), gooseQuery.getSearchSourceBuilder()), gooseQuery.getMetricField());
            gooseQueryResponseDTO.setErrCode("");
            gooseQueryResponseDTO.setMessage("");
        } catch (Throwable throwable) {
            gooseQueryResponseDTO.setErrCode("");
            gooseQueryResponseDTO.setMessage(throwable.getMessage());
            logger.error(StringTools.buildErrorMessage(throwable));
        }
        return gooseQueryResponseDTO;
    }

    //SQL ??????
    public QueryResult query(QueryRequestDTO dto) {
        String method = dto.getMethod();
        if("SQL".equals(method)){
            return queryBySQL(dto.getSql());
        } else {
            throw new RuntimeException(String.format("No such method supported: %s", method));
        }
    }

     private SQLQueryResult queryBySQL(String sql) {
        SQLQueryResult sqlQueryResult = new SQLQueryResult.Builder().time(System.currentTimeMillis()).build();
        try {
            //??????SQL
            QueryAction queryAction = searchDao.explain(sql);
            //??????
            Object result = QueryActionElasticExecutor.executeAnyAction(searchDao.getClient(), queryAction);
            //?????????????????????
            prepareSQLResult(result, sqlQueryResult);
            //??????????????????????????????????????????scroll_id??????, ??????????????????
            queryBySQLAndSave(result, sqlQueryResult, queryAction);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("queryBySql error", e);
            return null;
        }
        return sqlQueryResult;
    }

    /**
     *  ??????scroll_id????????????????????????
     */
    private void queryBySQLAndSave(Object firstScrollResult, SQLQueryResult sqlQueryResult,  QueryAction queryAction) throws Exception {
        if (queryAction instanceof DefaultQueryAction) {
            boolean isFirstScroll = true;
            String filePath = FileUtils.createNewFilePath(".csv");
            DefaultQueryAction defaultQueryAction = ((DefaultQueryAction) queryAction);
            AtomicBoolean isWritten = new AtomicBoolean(false);
            while (!StringUtils.isEmpty(defaultQueryAction.getForceScroll().getScrollId())) {
                CSVResult csvResult = csvResultsExtractor.extractResults(QueryActionElasticExecutor.executeAnyAction(searchDao.getClient(), defaultQueryAction), true, ",");
                if (csvResult.getHeaders().size() != 0) { //??????scroll_id??????????????????????????????csv??????
                    if(isFirstScroll) { //??????????????????scroll_id???????????????
                        fileService.save(csvResultsExtractor.extractResults(firstScrollResult, true, ","), filePath, isWritten);
                        //?????????????????????
                        sqlQueryResult.setFilePath(filePath);
                        isFirstScroll =false;
                    }
                    fileService.save(csvResult, filePath, isWritten);
                } else { //scroll????????????
                    break;
                }
            }
        }
    }

    /**
     * ??????SQLResult??????
     */
    private void prepareSQLResult(Object result, SQLQueryResult sqlQueryResult) throws Exception{
        ObjectResult objectResult = objectResultsExtractor.extractResults(result, false);
        sqlQueryResult.setResultColumns(Sets.newHashSet(objectResult.getHeaders()));
        List<Row> indexRowDatas = new ArrayList<>();
        for (List<Object> line : objectResult.getLines()) {
            Row indexRowData = new Row();
            for (int i = 0; i < objectResult.getHeaders().size(); i++) {
                indexRowData.put(objectResult.getHeaders().get(i), line.get(i));
            }
            indexRowDatas.add(indexRowData);
        }
        sqlQueryResult.setResultSize(indexRowDatas.size());
        sqlQueryResult.setTotal(indexRowDatas.size());
        sqlQueryResult.setResult(indexRowDatas);
        sqlQueryResult.setTime(System.currentTimeMillis() - sqlQueryResult.getTime());
    }
}
