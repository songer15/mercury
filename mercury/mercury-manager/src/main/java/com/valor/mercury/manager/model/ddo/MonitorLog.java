package com.valor.mercury.manager.model.ddo;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2019/11/25 17:36
 */
@Entity
@Table(name = "monitor_log")
public class MonitorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "level")
    private Integer level;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "source")
    private String source;

    @Column(name = "target_object")
    private String targetObject;

    @Column(name = "unique_value")
    private String uniqueValue;

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public String getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "MonitorLog{" + '\n' +
                "id=" + id + '\n' +
                ", createTime=" + createTime + '\n' +
                ", level=" + level + '\n' +
                ", detail='" + detail + '\n' +
                ", source='" + source + '\n' +
                ", targetObject='" + targetObject + '\n' +
                '}';
    }
}
