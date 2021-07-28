package com.valor.mercury.spider.model.jpa;


import javax.persistence.*;

@Entity
@Table(name = "BATCH_JOB_INSTANCE")
public class JobInstance {
    private Long id;
    private Long version;
    private String jobName;
    private String jobKey;
    private String status;
    private String startTime;
    private String pre;
    private String ter;

    @Transient
    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    @Transient
    public String getTer() {
        return ter;
    }

    public void setTer(String ter) {
        this.ter = ter;
    }

    @Transient
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Transient
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Id
    @Column(name = "JOB_INSTANCE_ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "JOB_NAME")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "JOB_KEY")
    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    @Override
    public String toString() {
        return "JobInstance{" +
                "id=" + id +
                ", version=" + version +
                ", jobName='" + jobName + '\'' +
                ", jobKey='" + jobKey + '\'' +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", pre='" + pre + '\'' +
                ", ter='" + ter + '\'' +
                '}';
    }
}
