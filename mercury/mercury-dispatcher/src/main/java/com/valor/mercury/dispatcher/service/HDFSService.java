package com.valor.mercury.dispatcher.service;


import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.HDFSClient;
import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class HDFSService extends RouteService{
    private static final Logger logger = LoggerFactory.getLogger(HDFSService.class);
    private HDFSClient hdfsClient;

    public HDFSService() throws Exception {
        dest = Router.Dest.HDFS;
        routeThreads = new RouteThread[ConfigTools3.getConfigAsInt("thread.hdfs.size",1)];
        hdfsClient = new HDFSClient(ConfigTools3.getString("hadoop.dfs.username"),
                ConfigTools3.getString("hadoop.dfs.nameservices"),
                ConfigTools3.getString("hadoop.core-site.conf"),
                ConfigTools3.getString("hadoop.hdfs-site.conf"));
        super.startRouteThread();
    }

    @Override
    public boolean route(List<Map<String, Object>> list, Router router) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map<String, Object> map : list) {
                String str = JsonUtils.toJsonString(map);
                if (str != null) {
                    stringBuilder.append(str);//追加内容
                }
                stringBuilder.append("\n");
            }
            String data = stringBuilder.toString();
            hdfsClient.write(data, router.path);
            if (!StringUtils.isEmpty(router.replacePath))
                hdfsClient.write(data, router.replacePath);
            return true;
        } catch (Exception e) {
            logger.error("send error", e);
            ServiceMonitor.getDefault().saveException(1, String.format("type: %s route error", router.type), e);
            return false;
        }
    }
}
