package com.valor.mercury.task.flink.devops.prt;

/**
 * @author Gavin
 * 2020/3/9 14:16
 */
public class PrtActionModel {

    private Double minReceivePeerTs;
    private Double connCount;
    private Double minSendTs;
    private Double minReceivePrtTs;
    private String seedId;
    private Double minReceivePiece;
    private Long actionTime;
    private Double minSendTime;
    private Double minReceiveRetryPiece;
    private String clientIp;
    private Double productType;
    private String channelId;
    private Double minSendFlow;

    public Double getMinSendFlow() {
        return minSendFlow;
    }

    public void setMinSendFlow(Double minSendFlow) {
        this.minSendFlow = minSendFlow;
    }

    public Double getMinReceivePeerTs() {
        return minReceivePeerTs;
    }

    public void setMinReceivePeerTs(Double minReceivePeerTs) {
        this.minReceivePeerTs = minReceivePeerTs;
    }

    public Double getConnCount() {
        return connCount;
    }

    public void setConnCount(Double connCount) {
        this.connCount = connCount;
    }

    public Double getMinSendTs() {
        return minSendTs;
    }

    public void setMinSendTs(Double minSendTs) {
        this.minSendTs = minSendTs;
    }

    public Double getMinReceivePrtTs() {
        return minReceivePrtTs;
    }

    public void setMinReceivePrtTs(Double minReceivePrtTs) {
        this.minReceivePrtTs = minReceivePrtTs;
    }

    public String getSeedId() {
        return seedId;
    }

    public void setSeedId(String seedId) {
        this.seedId = seedId;
    }

    public Double getMinReceivePiece() {
        return minReceivePiece;
    }

    public void setMinReceivePiece(Double minReceivePiece) {
        this.minReceivePiece = minReceivePiece;
    }

    public Long getActionTime() {
        return actionTime;
    }

    public void setActionTime(Long actionTime) {
        this.actionTime = actionTime;
    }

    public Double getMinSendTime() {
        return minSendTime;
    }

    public void setMinSendTime(Double minSendTime) {
        this.minSendTime = minSendTime;
    }

    public Double getMinReceiveRetryPiece() {
        return minReceiveRetryPiece;
    }

    public void setMinReceiveRetryPiece(Double minReceiveRetryPiece) {
        this.minReceiveRetryPiece = minReceiveRetryPiece;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Double getProductType() {
        return productType;
    }

    public void setProductType(Double productType) {
        this.productType = productType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "PrtActionModel{" +
                "minReceivePeerTs=" + minReceivePeerTs +
                ", connCount=" + connCount +
                ", minSendTs=" + minSendTs +
                ", minReceivePrtTs=" + minReceivePrtTs +
                ", seedId='" + seedId + '\'' +
                ", minReceivePiece=" + minReceivePiece +
                ", actionTime=" + actionTime +
                ", minSendTime=" + minSendTime +
                ", minReceiveRetryPiece=" + minReceiveRetryPiece +
                ", clientIp='" + clientIp + '\'' +
                ", productType=" + productType +
                ", channelId='" + channelId + '\'' +
                '}';
    }
}
