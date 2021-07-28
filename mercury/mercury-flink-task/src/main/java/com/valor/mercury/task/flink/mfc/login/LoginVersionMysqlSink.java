package com.valor.mercury.task.flink.mfc.login;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

/**
 * @author Gavin
 * 2019/10/14 15:59
 */
public class LoginVersionMysqlSink extends RichSinkFunction<LoginActionModel> {

    private DBClient client;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        client = DBClient.getInstance();
    }

    @Override
    public void invoke(LoginActionModel value, Context context) throws Exception {
        client.submitLoginActionModel(value);
    }

    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }

}
