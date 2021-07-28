package com.valor.mercury.common.model.db;


import com.valor.mercury.common.model.AbstractLMI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="message_count")
public class MessageCountDdo extends AbstractLMI {

    @Id
    @Column(name = "type")
    private String type;

    @Column(name = "received")
    private long received;

    @Column(name = "es_sent")
    private long esSent;

    @Column(name = "kafka_sent")
    private long kafkaSent;

    public MessageCountDdo(String type) {
        super();
        this.type = type;
    }

    public MessageCountDdo() {
        super();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getReceived() {
        return received;
    }

    public void setReceived(long received) {
        this.received = received;
    }

    public long getEsSent() {
        return esSent;
    }

    public void setEsSent(long esSent) {
        this.esSent = esSent;
    }

    public long getKafkaSent() {
        return kafkaSent;
    }

    public void setKafkaSent(long kafkaSent) {
        this.kafkaSent = kafkaSent;
    }
}
