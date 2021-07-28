package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2019/11/25 17:09
 */
@Entity
@Table(name = "offline_meta_task")
public class OffLineMetaTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
    private Date lastModifyTime;

    @Column(name = "description")
    private String desc;

    @Column(name = "type")
    private String type;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "inc_value")
    private String inc;

    @Column(name = "executor")
    private String executor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getInc() {
        return inc;
    }

    public void setInc(String inc) {
        this.inc = inc;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    @Override
    public String toString() {
        return "OffLineMetaTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", desc='" + desc + '\'' +
                ", type='" + type + '\'' +
                ", config='" + config + '\'' +
                ", inc='" + inc + '\'' +
                ", executor='" + executor + '\'' +
                '}';
    }
}
