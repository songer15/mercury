package com.valor.mercury.manager.model.ddo;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2020/9/8 15:04
 */
@Entity
@Table(name = "etl_data_traffic")
public class ETLDataTraffic {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "type")
    private String type;

    @Column(name = "data_size")
    private Integer num;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
