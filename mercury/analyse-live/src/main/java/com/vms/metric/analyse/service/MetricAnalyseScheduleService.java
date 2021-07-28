package com.vms.metric.analyse.service;

import com.vms.metric.analyse.config.MetricAnalyseConfig;
import com.vms.metric.analyse.model.Response;
import com.vms.metric.analyse.model.ResponseEntity;
import com.vms.metric.analyse.model.WorkItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
public class MetricAnalyseScheduleService {

    private static Logger logger = LoggerFactory.getLogger(MetricAnalyseScheduleService.class);

    private static final long SCHEDULE_TIME = 60_000;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MetricAnalyseExecuteService executeService;

    /**
     * 请求调度服务中心获得需要执行的任务
     *
     * @return
     */
    @Scheduled(fixedDelay = SCHEDULE_TIME)
    public void seekJobs() {

        //参数设置
        String serverUrl = MetricAnalyseConfig.getServerUrlRequest();
        Response response = new Response();
        response.setKey(MetricAnalyseConfig.getHttpAuthenticationKey());
        response.setPassword(MetricAnalyseConfig.getHttpAuthenticationPassword());
        response.setServerId(MetricAnalyseConfig.getLocalServerId());

        //获取任务数据
        try {
            String str = netRequest(serverUrl, objectMapper.writeValueAsString(response));
            if (Strings.isNotEmpty(str)) {
                ResponseEntity<List<WorkItem>> responseEntity = objectMapper.readValue(str, new TypeReference<ResponseEntity<List<WorkItem>>>() {
                });
                if (responseEntity.getCode() != 200)
                    logger.error("seekJobs error: code:{},msg:{}", responseEntity.getCode(), responseEntity.getMsg());
                else {
                    List<WorkItem> workItems = responseEntity.getData();
                    for (WorkItem workItem : workItems)
                        executeService.addQueue(workItem);  //放入任务队列排队执行
                }
            } else {
                logger.error("seek Task error: response is null");
            }
        } catch (IOException e) {
            logger.error("http post request error:{}", e);
        } catch (Exception e) {
            logger.error("http format response error:{}", e);
        }

    }

    public static String netRequest(String serverUrl, String param) throws IOException {

        StringBuffer sb = new StringBuffer();
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(60_000);
        connection.setReadTimeout(60_000);

        //设置请求属性
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
        connection.setRequestProperty("Charset", "UTF-8");
        connection.connect();

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(param.getBytes());
        out.flush();
        out.close();

        //获得响应状态
        int resultCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == resultCode) {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
        }
        connection.disconnect();
        return sb.toString();
    }

}
