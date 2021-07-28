package com.valor.mercury.manager.tool;


import com.mfc.config.ConfigTools3;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * 对 mercury的get请求
     */
    public static String httpGet(List<String> serverUrls, String  requestParams) throws IOException {
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
