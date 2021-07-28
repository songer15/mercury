package com.valor.mercury.script.task

import com.google.common.collect.Lists
import com.valor.mercury.common.client.EsClient
import com.valor.mercury.common.model.Router
import com.valor.mercury.script.MetricAnalyse
/**
 * 除了当前索引，关闭所有其他索引
 */
class CloseIndices implements MetricAnalyse{

    @Override
    void run(String[] args) throws Exception {
        //不能关闭的，有特殊命名的索引
        Set<String> shouldNotBeClose = new HashSet<>();
        for (String index : args) {
            shouldNotBeClose.add(index)
        }
        shouldNotBeClose.add("mfc_user_info-2018-server")

        EsClient esClient = new EsClient(Lists.newArrayList("http://metric01.mecury0data.xyz:9201"));

        Map<String, Router.IndexNameStrategy> suffice = new HashMap<>();
        suffice.put(Router.IndexNameStrategy.DAY.getIndexNameSuffix(), Router.IndexNameStrategy.DAY);
        suffice.put(Router.IndexNameStrategy.WEEK.getIndexNameSuffix(), Router.IndexNameStrategy.WEEK);
        suffice.put(Router.IndexNameStrategy.MONTH.getIndexNameSuffix(), Router.IndexNameStrategy.MONTH );
        suffice.put(Router.IndexNameStrategy.QUARTER.getIndexNameSuffix(), Router.IndexNameStrategy.QUARTER );
        suffice.put(Router.IndexNameStrategy.YEAR.getIndexNameSuffix(), Router.IndexNameStrategy.YEAR);

        Set<String> rollingIndices = esClient.getIndicesByPattern("*-server");

        Set<String> shouldBeClose = new HashSet<>();
        Set<String> shouldBeOpen = new HashSet<>();

        for (String index : rollingIndices) {

            String prefix = index.substring(0, index.indexOf("-20"));

            boolean shouldClose = true;
            for (String suffix : suffice.keySet()) {
                Router.IndexNameStrategy strategy = suffice.get(suffix);
                //当该索引包含当前时间的后缀，且它是当前最大索引，保持open
                if (index.contains(suffix) && !rollingIndices.contains(prefix + strategy.getIndexNameSuffix(null,1))) {
                    shouldClose = false;
                    break;
                }
            }
            if (shouldNotBeClose.contains(index)) {
                shouldClose = false;
            }
            if (shouldClose) {
                shouldBeClose.add(index);
            } else {
                shouldBeOpen.add(index);
            }
        }

        List<String> toClose = new ArrayList<>()
        List<String> toOpen = new ArrayList<>()
        for (String index : shouldBeClose) {
            if ("open".equals(esClient.getIndexStatus(index))) {
                System.out.println(index + "  should be  close");
                toClose.add(index);
            }
        }

        for (String index : shouldBeOpen) {
            if ("close".equals(esClient.getIndexStatus(index))) {
                System.out.println(index + "  should be  open");
                toOpen.add(index);
            }
        }


        //批量关闭索引
        esClient.closeIndices(toClose.toArray(new String[toClose.size()]))
    }

}
