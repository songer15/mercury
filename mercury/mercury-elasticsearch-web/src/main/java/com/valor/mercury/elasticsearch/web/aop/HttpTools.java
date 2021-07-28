package com.valor.mercury.elasticsearch.web.aop;



import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;


public class HttpTools {

    public static String getRemoteHost(HttpServletRequest request) {
        String remoteHost = null;
        //ipAddress = this.getRequest().getRemoteAddr();
        remoteHost = request.getHeader("x-forwarded-for");
        if (remoteHost == null || remoteHost.length() == 0 || "unknown".equalsIgnoreCase(remoteHost)) {
            remoteHost = request.getHeader("CF-Connecting-IP");
        }
        if (remoteHost == null || remoteHost.length() == 0 || "unknown".equalsIgnoreCase(remoteHost)) {
            remoteHost = request.getHeader("Proxy-Client-IP");
        }
        if (remoteHost == null || remoteHost.length() == 0 || "unknown".equalsIgnoreCase(remoteHost)) {
            remoteHost = request.getHeader("WL-Proxy-Client-IP");
        }
        if (remoteHost == null || remoteHost.length() == 0 || "unknown".equalsIgnoreCase(remoteHost)) {
            remoteHost = request.getRemoteAddr();
        }

        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (remoteHost != null && remoteHost.length() > 15) { //"***.***.***.***".length() = 15
            if (remoteHost.indexOf(",") > 0) {
                remoteHost = remoteHost.substring(0, remoteHost.indexOf(","));
            }
        }

        if (StringUtils.isEmpty(remoteHost)) {
            remoteHost = "unknown host";
        }

        return remoteHost;
    }


}
