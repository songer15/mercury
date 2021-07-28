package com.valor.mercury.dispatcher.service;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.InfluxDBClient;
import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.Router;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class InfluxDBService extends RouteService{
    private InfluxDBClient client;

    public InfluxDBService() {
        dest = Router.Dest.INFLUXDB;
        needBatchSend = true;
        client = new InfluxDBClient();
        client.setInfluxDBRelay(ConfigTools3.getString("influxdb.relay.url"), 100, 5, TimeUnit.SECONDS);
        client.setInfluxDBCluster((ConfigTools3.getAsList("influxdb.cluster.urls")), 100, 5, TimeUnit.SECONDS);
        routeThreads = new RouteThread[ConfigTools3.getConfigAsInt("thread.influxdb.size",1)];
        super.startRouteThread();
    }

    @Override
    public boolean route(List<Map<String, Object>> list, Router router){
        try {
            client.write(list, router.database, router.measurement, router.tags.split(","));
            return true;
        } catch (Exception e) {
            ServiceMonitor.getDefault().saveException(1, String.format("type: %s route error", router.type), e);
            return false;
        }
    }

}
