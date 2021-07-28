package com.valor.mercury.task.flink.devops.pm;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gavin
 * 2020/4/10 17:15
 * 计算TOP N
 */
public class TopNItemsProcess extends KeyedProcessFunction<String, MFCPMTopQueryResult, MFCPMTopQueryResult> {

    private final int topSize;
    private ListState<MFCPMTopQueryResult> itemState;
    private Long timestamp = null;
    private String tag;

    public TopNItemsProcess(int topSize, String tag) {
        this.topSize = topSize;
        this.tag = tag;
    }

    @Override
    public void processElement(MFCPMTopQueryResult value, Context ctx, Collector<MFCPMTopQueryResult> out) throws Exception {
        itemState.add(value);
//        System.out.println(tag + ": " + value.toString());
        if (timestamp == null) {
            timestamp = System.currentTimeMillis() + 180_000;
            System.out.println(tag + " registerEventTimeTimer:" + timestamp);
            ctx.timerService().registerProcessingTimeTimer(timestamp);
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        //状态注册
        ListStateDescriptor<MFCPMTopQueryResult> itemViewStateDesc = new ListStateDescriptor<>(
                "MFCPMTopQueryResult-state", MFCPMTopQueryResult.class
        );
        itemState = getRuntimeContext().getListState(itemViewStateDesc);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<MFCPMTopQueryResult> collector) throws Exception {
        this.timestamp = null;
        System.out.println(tag + " onTimer");
        List<MFCPMTopQueryResult> allItems = new ArrayList<>();
        for (MFCPMTopQueryResult item : itemState.get()) {
            allItems.add(item);
        }
        System.out.println(tag + " result size:" + allItems.size());
        itemState.clear();
        allItems.sort((v1, v2) -> (int) (v2.getUniqueCount() - v1.getUniqueCount()));
        int writeSize = allItems.size() > topSize ? topSize : allItems.size();
        for (int i = 0; i < writeSize; i++) {
//            System.out.println(tag + ": " + allItems.get(i).toString());
            collector.collect(allItems.get(i));
        }
    }

}
