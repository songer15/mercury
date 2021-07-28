package com.valor.mercury.task.fink.live.source;//package com.vms.metric.flink.live.source;
//
//
//import com.vms.metric.flink.live.java.ESQueryService;
//import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
//
//import java.util.List;
//import java.util.Map;
//
//public class ElasticSearchSource extends RichSourceFunction<Map<String, Object>> {
//    private ESQueryService queryService;
//    private Configuration config;
//
//    public ElasticSearchSource(Configuration cfg) {
//        this.config = cfg;
//    }
//
//    @Override
//    public void run(SourceContext<Map<String, Object>> ctx) throws Exception {
//
//        Map<String, Object> dataMap = queryService.queryEsData(config.SearchSource, config.indexName);
//        List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("data");
//        dataList.forEach(o -> ctx.collect(o));
//    }
//
//    @Override
//    public void cancel() {
//
//
//    }
//}
