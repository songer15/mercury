//package com.valor.mercury.manager.test;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.google.gson.JsonArray;
//import com.valor.mercury.common.util.JsonUtil;
//import com.valor.mercury.common.util.PostUtil;
//import com.valor.mercury.manager.config.ConstantConfig;
//import com.valor.mercury.manager.model.system.HDFSMetric;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.statement.select.Join;
//import net.sf.jsqlparser.statement.select.PlainSelect;
//import net.sf.jsqlparser.statement.select.Select;
//import net.sf.jsqlparser.statement.select.SelectItem;
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.ZooDefs;
//import org.apache.zookeeper.ZooKeeper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * @author Gavin
// * 2020/7/24 14:17
// */
//public class Test {
//    private Logger logger = LoggerFactory.getLogger(Test.class);
//
//    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'日'HH'时'");
//
//    @org.junit.Test
//    public void test() throws Exception {
//        String value = PostUtil.httpGet(Collections.singletonList("http://51.79.17.96:10002/")
//                , "");
////        JSONObject object = JSONObject.parseObject(value).getJSONObject("clusterMetrics");
////        HashMap map=JSONObject.parseObject(object.toJSONString(),HashMap.class);
//        logger.info(value);
//    }
//
//    @org.junit.Test
//    public void testSQL() throws Exception {
//        String sql = "select a.c1,b.c2,c.* from tbl_a a left join tbl_b b on a.c3=b.c3 left join tbl_c c on a.c4=b.c4 where c.c5='tbl_d'";
//        Select stmt = (Select) CCJSqlParserUtil.parse(sql);
//        PlainSelect selectBody = (PlainSelect) stmt.getSelectBody();
//        for (SelectItem item : selectBody.getSelectItems())
//            System.out.println(item.toString());
//        System.out.println(selectBody.getFromItem().toString());
//        for (Join item : selectBody.getJoins())
//            System.out.println(item.toString());
//        System.out.println(selectBody.getWhere().toString());
//    }
//
//    @org.junit.Test
//    public void testIP() throws Exception {
//        HDFSMetric metric=new HDFSMetric();
//        Class clazz=metric.getClass();
//        logger.info(clazz.getName()+"");
//    }
//
//    public static String getRealIp() throws SocketException {
//        String localip = null;// 本地IP，如果没有配置外网IP则返回它
//        String netip = null;// 外网IP
//
//        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
//        InetAddress ip = null;
//        boolean finded = false;// 是否找到外网IP
//        while (netInterfaces.hasMoreElements() && !finded) {
//            NetworkInterface ni = netInterfaces.nextElement();
//            Enumeration<InetAddress> address = ni.getInetAddresses();
//            while (address.hasMoreElements()) {
//                ip = address.nextElement();
//                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
//                    netip = ip.getHostAddress();
//                    finded = true;
//                    break;
//                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
//                        && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
//                    localip = ip.getHostAddress();
//                }
//            }
//        }
//
//        if (netip != null && !"".equals(netip)) {
//            return netip;
//        } else {
//            return localip;
//        }
//    }
//
//    @org.junit.Test
//    public void testZK() throws Exception {
//        ZooKeeper zkClient = new ZooKeeper(ConstantConfig.zkClusterPath(), 15000, event -> {
//            if (Watcher.Event.KeeperState.Disconnected.equals(event.getState())) {
//                logger.error("Zookeeper Disconnected!");
//            }
//        });
//        zkClient.create("/test", ConstantConfig.localIP().getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
//
//    }
//}
