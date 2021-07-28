package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2020/10/22 14:04
 */
@Entity
@Table(name = "hive_table")
public class HiveTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "local_create_time")
    private Date localCreateTime;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
    private Date lastModifyTime;

    @Column(name = "name")
    private String name;

    @Column(name = "db_name")
    private String database;

    @Column(name = "cron")
    private String cron;

    @Column(name = "command")
    private String command;

    @Column(name = "location")
    private String location;

    @Column(name = "owner")
    private String owner;

    @Column(name = "table_type")
    private String tableType;

    @Column(name = "status")
    private String status;

    @Column(name = "last_execute_time")
    private Date lastExecuteTime;

    @Column(name = "next_execute_time")
    private Date nextExecuteTime;

    public Date getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(Date lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    public Date getNextExecuteTime() {
        return nextExecuteTime;
    }

    public void setNextExecuteTime(Date nextExecuteTime) {
        this.nextExecuteTime = nextExecuteTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLocalCreateTime() {
        return localCreateTime;
    }

    public void setLocalCreateTime(Date localCreateTime) {
        this.localCreateTime = localCreateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
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

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "HiveTable{" +
                "id=" + id +
                ", localCreateTime=" + localCreateTime +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", name='" + name + '\'' +
                ", database='" + database + '\'' +
                ", cron='" + cron + '\'' +
                ", command='" + command + '\'' +
                ", location='" + location + '\'' +
                ", owner='" + owner + '\'' +
                ", tableType='" + tableType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
