package com.valor.mercury.common.model.db;


import com.valor.mercury.common.model.AbstractLMI;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "error_report")
public class ErrorReport extends AbstractLMI {


    @Id
    @Column(name = "id")
    @GenericGenerator(name = "idGenerator", strategy = "identity")
    @GeneratedValue(generator = "idGenerator")
    private Long id;

    @Column(name = "task_name")
    private String taskName;  //任务名

    @Column(name = "server_id")
    private long serverId; //所属服务的Id

    @Column(name="msg", columnDefinition = "text")
    private String msg;

    @Column(name = "execute_regulation")
    private String executeRegulation;     //执行调度规则

    @Column(name = "type")
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getExecuteRegulation() {
        return executeRegulation;
    }

    public void setExecuteRegulation(String executeRegulation) {
        this.executeRegulation = executeRegulation;
    }

    public ErrorReport() {

    }
}
