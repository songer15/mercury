package com.valor.mercury.manager.model.ddo;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author Gavin
 * 2019/11/25 17:22
 */
@Entity
@Table(name = "offline_task")
public class OffLineTask {
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

    @Column(name = "status")
    private String status;

    @Column(name = "enable")
    private String enable;

    @Column(name = "cron")
    private String cron;

    @Column(name = "fail_times")
    private Integer failTimes = 0;

    @Column(name = "success_times")
    private Integer successTimes = 0;

//    @Column(name = "last_execute_time")
//    private Date lastExecuteTime;

    @Column(name = "next_execute_time")
    private Date nextExecuteTime;

    @Column(name = "running_batch")
    private Integer runningBatch;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "dag", columnDefinition = "TEXT")
    private String dag;

    @Transient
    private List<DAG> dags;

    public Integer getRunningBatch() {
        return runningBatch;
    }

    public void setRunningBatch(Integer runningBatch) {
        this.runningBatch = runningBatch;
    }

    public List<DAG> getDags() {
        return dags;
    }

    public void setDags(List<DAG> dags) {
        this.dags = dags;
    }

    public String getDag() {
        return dag;
    }

    public void setDag(String dag) {
        this.dag = dag;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Integer getFailTimes() {
        return failTimes;
    }

    public void setFailTimes(Integer failTimes) {
        this.failTimes = failTimes;
    }

    public Integer getSuccessTimes() {
        return successTimes;
    }

    public void setSuccessTimes(Integer successTimes) {
        this.successTimes = successTimes;
    }

//    public Date getLastExecuteTime() {
//        return lastExecuteTime;
//    }

//    public void setLastExecuteTime(Date lastExecuteTime) {
//        this.lastExecuteTime = lastExecuteTime;
//    }

    public Date getNextExecuteTime() {
        return nextExecuteTime;
    }

    public void setNextExecuteTime(Date nextExecuteTime) {
        this.nextExecuteTime = nextExecuteTime;
    }

    public boolean isEmpty() {
        return Strings.isEmpty(name) || Strings.isEmpty(cron);
    }

    @Override
    public String toString() {
        return "OffLineTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", desc='" + desc + '\'' +
                ", status='" + status + '\'' +
                ", enable='" + enable + '\'' +
                ", cron='" + cron + '\'' +
                ", failTimes=" + failTimes +
                ", successTimes=" + successTimes +
//                ", lastExecuteTime=" + lastExecuteTime +
                ", nextExecuteTime=" + nextExecuteTime +
                ", runningBatch=" + runningBatch +
                ", dag='" + dag + '\'' +
                ", dags=" + dags +
                '}';
    }
}
