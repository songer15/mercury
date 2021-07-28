package com.valor.mercury.common.util;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.model.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class PostUtil {

    private static Logger logger = LoggerFactory.getLogger(PostUtil.class);


    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    public static List<ExecutorCommand> getCommandsFromMercury(String status) {
        return getCommandsFromMercury(getClientName(), getClientPassword(), status);
    }

    public static List<ExecutorCommand> getCommandsFromMercury(String clientName, String clientPsd, String status) {
        List<ExecutorCommand> commands = new ArrayList<>();
        try {
            String str = PostUtil.httpGet(getRequestUrls(),
                    "clientName" + "=" + clientName + "&"
                            + "clientPsd" + "=" + clientPsd + "&"
                            + "status" + "=" + status);
            if (!(str == null || "".equals(str))) {
                JsonResult jsonResult = JsonUtil.objectMapper.readValue(str, new TypeReference<JsonResult>(){});
                if (200 == (Integer) jsonResult.get("code")) {
                    List<Map> list = (List<Map>) jsonResult.get("commands");
                    for (Map map : list)
                        commands.add(JSON.parseObject(JSON.toJSONString(map), ExecutorCommand.class));
                }
            }
        } catch (Exception ex) {
            logger.error("get commands error: ", ex);
        }
        return commands;
    }

    public static void reportToMercury(ExecutorReport executorReport) {
        try {
            logger.info("send executorReport:{}", executorReport.toString());
            String str = PostUtil.httpPost(getReportUrls(), JsonUtil.objectMapper.writeValueAsString(executorReport));
            if (!(str == null || "".equals(str))) {
                JsonResult jsonResult = JsonUtil.objectMapper.readValue(str, JsonResult.class);
                int code = (int) jsonResult.get("code");
                if (code != 200)
                    logger.info("executorReport error : {}", jsonResult.get("msg"));
                else
                    logger.info("send executorReport success");
            }
        } catch (Exception e) {
            logger.error("send executorReport error", e);
        }
    }

    public static File downloadFromMercury(String clientName, String clientPsd, String fileMd5, String filePath) throws IOException {
        try {
            File file = new File(filePath);
            List<String> serverUrls = getDownloadUrls();
            String  requestParams = "clientName" + "=" + clientName + "&"
                    + "clientPsd" + "=" + clientPsd + "&"
                    + "fileMd5" + "=" + fileMd5;
            Iterator<String> iterator = serverUrls.iterator();
            boolean success = false;
            while (iterator.hasNext()) {
                StringBuilder serverUrl = new StringBuilder(iterator.next());
                if (!StringUtils.isEmpty(requestParams)) {
                    serverUrl.append("?");
                    serverUrl.append(requestParams);
                }
                logger.info(serverUrl.toString());

                HttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                HttpGet httpGet = new HttpGet(serverUrl.toString());
                HttpResponse response = httpClient.execute(httpGet);

                logger.info("download status: {}", response.getStatusLine());

                InputStream in = response.getEntity().getContent();

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int length;
                while((length = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                return file;

            }
            return null;
        } catch (Exception ex) {
            logger.error("download error: ", ex);
            return null;
        }
    }

    public static ExecutorReport buildReport(String taskType, Long instanceId, Map<String, Object> metrics, String jobsStatus, Object error) {
        ExecutorReport report = new ExecutorReport();
        report.setClientName(PostUtil.getClientName());
        report.setClientPsd(PostUtil.getClientPassword());
        report.setInstanceID(instanceId);
        report.setInstanceStatus(jobsStatus);
        report.setActionTime(new Date());
        report.setTaskType(taskType);
        report.setMetrics(metrics);
        if (error != null)
            report.setErrorMessage(StringTools.buildErrorMessage(error));
        return report;
    }


    /**
     * 对 mercury的get请求
     */
    public static String httpGet(List<String> serverUrls, String  requestParams)  {
        Iterator<String> iterator = serverUrls.iterator();
        StringBuffer sb = new StringBuffer();
        boolean success = false;
        while (iterator.hasNext()) {
            try {
                StringBuilder serverUrl = new StringBuilder(iterator.next());
                if (!StringUtils.isEmpty(requestParams)) {
                    serverUrl.append("?");
                    serverUrl.append(requestParams);
                }
                URL url = new URL(serverUrl.toString());
                logger.info(serverUrl.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000 * 60);
                connection.setReadTimeout(60 * 1000);

                //设置请求属性
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                connection.setRequestProperty("Charset", "UTF-8");
                connection.connect();

                //获得响应状态
                int resultCode = connection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == resultCode) {
                    success = true;
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                }
                connection.disconnect();
                if (success)
                    return sb.toString();
            } catch (Exception ex) {
                logger.error("httpGet error: ", ex);
                continue;
            }
        }
        return null;
    }

    /**
     * 对mercury的post请求
     */
    public static String httpPost(List<String> serverUrls, String requestBody) throws IOException {
        Iterator<String> iterator = serverUrls.iterator();
        StringBuffer sb = new StringBuffer();
        boolean success = false;
        while (iterator.hasNext()) {
            try {
                String serverUrl = iterator.next();
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(1000 * 60);
                connection.setReadTimeout(60 * 1000);

                //设置请求属性
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                connection.setRequestProperty("Charset", "UTF-8");
                connection.connect();

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(requestBody.getBytes());
                out.flush();
                out.close();

                //获得响应状态
                int resultCode = connection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == resultCode) {
                    success = true;
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                }
                connection.disconnect();
                if (success)
                    return sb.toString();
            } catch (Exception ex) {
                logger.error("httpPost error: ", ex);
                continue;
            }
        }
        return null;
    }

    public static List<String> getRequestUrls() {
        List<String> urls = new ArrayList<>();
        List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
        for (String ip : ips)
            urls.add("http://" + ip + ConfigTools3.getConfigAsString("mercury.url.command.path"));
        return urls;
    }

    public static List<String> getReportUrls() {
        List<String> urls = new ArrayList<>();
        List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
        for (String ip : ips)
            urls.add("http://" + ip + ConfigTools3.getConfigAsString("mercury.url.report.path"));
        return urls;
    }

    public static List<String> getDownloadUrls() {
        List<String> urls = new ArrayList<>();
        List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
        for (String ip : ips)
            urls.add("http://" + ip + ConfigTools3.getConfigAsString("mercury.url.download.path"));
        return urls;
    }

    public static String getClientName() { return ConfigTools3.getConfigAsString("mercury.client.name"); }

    public static String getClientPassword() {
        return ConfigTools3.getString("mercury.client.password");
    }

}
