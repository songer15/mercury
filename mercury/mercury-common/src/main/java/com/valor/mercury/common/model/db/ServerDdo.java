package com.valor.mercury.common.model.db;

import com.valor.mercury.common.model.AbstractLMI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "server")
public class ServerDdo extends AbstractLMI {


    @Id
    @Column(name = "id")
    private Long id;  //所属服务Id

    @NotNull
    @Column(name = "server_name")
    private String serverName;  //所属服务名字

    @Column(name = "auth_key")
    private String authKey;  // 连接过程需要验证的key

    @Column(name = "auth_password")
    private String authPassword;  // 连接过程需要验证的密码

    @Column(name = "enable", columnDefinition = "tinyint(1) default 1")
    private boolean enable; //是否启用

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
