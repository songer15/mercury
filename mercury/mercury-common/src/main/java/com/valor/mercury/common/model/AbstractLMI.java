package com.valor.mercury.common.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
/**
 * 最后修改信息
 */
@MappedSuperclass
public abstract class AbstractLMI extends AbstractPrintable implements Serializable {


    private static final long serialVersionUID = 6191746761874799049L;
    @CreationTimestamp
    @Column(name = "create_time",updatable = false,columnDefinition = "timestamp default CURRENT_TIMESTAMP", nullable = false)
    private Timestamp createTime = new Timestamp(new Date().getTime());

    @Column(name = "last_modify_user", columnDefinition = "varchar(64) default ''")
    private String lastModifyUser = "auto";

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    //@Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp lastModifyTime = new Timestamp(new Date().getTime());

    @Column(name = "description", columnDefinition = "varchar(512) default ''")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public String getLastModifyUser() {
        return lastModifyUser;
    }

    public Timestamp getLastModifyTime() {
        return lastModifyTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }

    public void setLastModifyTime(Timestamp lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

}