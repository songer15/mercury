package com.valor.mercury.task.flink.mfc.login;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Gavin
 * 2019/10/12 16:44
 */
public class LoginVersionProcessFunction extends ProcessWindowFunction<LoginActionModel, LoginActionModel, String, TimeWindow> {

    private Map<String, Tuple2<Long, Long>> macMap = new HashMap<>();
    private DBClient client;
    private final OutputTag<UpgradeActionModel> upgradeTag = new OutputTag<UpgradeActionModel>("mfc-upgrade") {
    };
    private final int dataExpireTime = 2 * 24 * 60 * 60_000;//数据过期时间两天

    public OutputTag<UpgradeActionModel> getUpgradeTag() {
        return upgradeTag;
    }

    @Override
    public void process(String s, Context context, Iterable<LoginActionModel> elements, Collector<LoginActionModel> out) throws Exception {
        if (client == null)
            client = DBClient.getInstance();
        Long appVersion;
        if (!macMap.containsKey(s)) {
            appVersion = client.queryMFCAppVersion("SELECT appVersion FROM mfc_user WHERE did ='" + s + "'");
            if (appVersion != null)
                macMap.put(s, new Tuple2<>(appVersion, System.currentTimeMillis()));
        } else
            appVersion = macMap.get(s).f0;

        LoginActionModel pre = null;
        LoginActionModel current;
        Iterator<LoginActionModel> iterator = elements.iterator();
        while (iterator.hasNext()) {
            current = iterator.next();
            if (pre == null) {
                //开始遍历
                pre = current;
                if (appVersion == null) {
                    //数据库从未添加过此条数据
                    appVersion = current.getAppVersion();
                    macMap.put(s, new Tuple2<>(appVersion, System.currentTimeMillis()));
                } else if (!current.getAppVersion().equals(appVersion))
                    //检测到版本升级，生成升级事件数据，提交到旁路数据流
                    context.output(upgradeTag, new UpgradeActionModel(current.getLoginType(), current.getUserId(), current.getDevice(), current.getActionTime(), current.getVendorId(), current.getAppVersion(), appVersion, current.getDid()));
            } else {
                if (!current.getAppVersion().equals(pre.getAppVersion()))
                    //检测到版本升级，生成升级事件数据，提交到旁路数据流
                    context.output(upgradeTag, new UpgradeActionModel(current.getLoginType(), current.getUserId(), current.getDevice(), current.getActionTime(), current.getVendorId(), current.getAppVersion(), pre.getAppVersion(), current.getDid()));
                pre = current;
            }
        }
        //更新mac-appVersion-Timestamp
        if (pre != null) {
            macMap.put(s, new Tuple2<>(pre.getAppVersion(), System.currentTimeMillis()));
            //将此用户的最新登录信息输出到主数据流
            out.collect(pre);
        }
        macMap.entrySet().removeIf(v -> System.currentTimeMillis() - v.getValue().f1 > dataExpireTime);
    }
}
