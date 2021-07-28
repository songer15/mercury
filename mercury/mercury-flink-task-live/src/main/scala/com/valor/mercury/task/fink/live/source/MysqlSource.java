package com.valor.mercury.task.fink.live.source;//package com.vms.metric.flink.live.source;
//
//import com.mysql.jdbc.Connection;
//import com.mysql.jdbc.PreparedStatement;
//import org.apache.flink.api.java.tuple.Tuple10;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
//
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//
//public class MysqlSource extends RichSourceFunction<Tuple10<String, String, String, String, String, String, String, String, String, String>> {
//
//    private Connection connect = null;
//    private PreparedStatement ps = null;
//
//    @Override
//    public void open(Configuration parameters) throws Exception {
//        super.open(parameters);
//        Class.forName("com.mysql.jdbc.Driver");
//        connect = (Connection) DriverManager.getConnection("jdbc:mysql://192.168.21.11:3306", "root", "flink");
//        ps = (PreparedStatement) connect
//                .prepareStatement("select col1,col2,col3,col4,col5,col6,col7,col8,col9,col10 from flink.test_tb");
//    }
//
//
//    @Override
//    public void run(
//            SourceContext<Tuple10<String, String, String, String, String, String, String, String, String, String>> collect)
//            throws Exception {
//        ResultSet resultSet = ps.executeQuery();
//        while (resultSet.next()) {
//            Tuple10<String, String, String, String, String, String, String, String, String, String> tuple = new Tuple10<String, String, String, String, String, String, String, String, String, String>();
//            tuple.setFields(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
//                    resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
//                    resultSet.getString(8), resultSet.getString(9), resultSet.getString(10));
//            collect.collect(tuple);
//        }
//    }
//
//    @Override
//    public void cancel() {
//        try {
//            super.close();
//            if (connect != null) {
//                connect.close();
//            }
//            if (ps != null) {
//                ps.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
