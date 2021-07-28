package com.valor.mercury.spider.model;

import com.valor.mercury.common.model.AbstractPrintable;

import java.util.Objects;

public class MysqlDataSource extends AbstractPrintable {
    public String name;
    public String url;
    public String userName;
    public String password;
    public String driver = "com.mysql.cj.jdbc.Driver";


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MysqlDataSource that = (MysqlDataSource) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(url, that.url) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password) &&
                Objects.equals(driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, userName, password, driver);
    }
}
