package com.valor.mercury.task.flink.mfc.login;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple8;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gavin
 * 2019/10/14 17:00
 */
public class LoginVersionAggregateFunction implements AggregateFunction<LoginActionModel, Tuple8<Integer, Integer, Date, Long, String, String, String, String>, Tuple8<Integer, Integer, Date, Long, String, String, String, String>> {

    private Set<String> userSet = new HashSet<>();

    @Override
    public Tuple8<Integer, Integer, Date, Long, String, String, String, String> createAccumulator() {
        userSet.clear();
        Tuple8<Integer, Integer, Date, Long, String, String, String, String> accumulator = new Tuple8<>();
        accumulator.setField(0, 0);
        accumulator.setField(0, 1);
        return accumulator;
    }

    @Override
    public Tuple8<Integer, Integer, Date, Long, String, String, String, String> add(LoginActionModel value, Tuple8<Integer, Integer, Date, Long, String, String, String, String> accumulator) {
        if (accumulator.f2 == null) {
            //开始计算
            accumulator.f2 = new Date(value.getActionTime());
            accumulator.f3 = value.getAppVersion(); //version
            accumulator.f4 = value.getCountryCode(); //countryCode
            accumulator.f5 = value.getDevice(); //device
            accumulator.f6 = value.getLoginType(); //loginType
            accumulator.f7 = value.getMacSub(); //macSub
        }
        accumulator.f0++;  //count
        if (!userSet.contains(value.getUserId())) {
            userSet.add(value.getUserId());
            accumulator.f1++;  //uniqueCount
        }
        return accumulator;
    }

    @Override
    public Tuple8<Integer, Integer, Date, Long, String, String, String, String> getResult(Tuple8<Integer, Integer, Date, Long, String, String, String, String> accumulator) {
        userSet.clear();
        return accumulator;
    }

    @Override
    public Tuple8<Integer, Integer, Date, Long, String, String, String, String> merge(Tuple8<Integer, Integer, Date, Long, String, String, String, String> a, Tuple8<Integer, Integer, Date, Long, String, String, String, String> b) {
        return null;
    }
}
