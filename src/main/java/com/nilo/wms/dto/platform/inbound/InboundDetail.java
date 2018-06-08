package com.nilo.wms.dto.platform.inbound;

import com.alibaba.fastjson.annotation.JSONField;
import com.nilo.wms.common.BaseDo;


public class InboundDetail extends BaseDo<Long> {


    private String  clientCode;
    private String  referenceNo;
    private String  sku;
    @JSONField(name = "goods_num")
    private Integer qty;
    private Integer receiveQty;
    private Integer receiveTime;
    private Short   status;

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getReceiveQty() {
        return receiveQty;
    }

    public void setReceiveQty(Integer receiveQty) {
        this.receiveQty = receiveQty;
    }

    public Integer getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Integer receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InboundDetail{" +
                "clientCode='" + clientCode + '\'' +
                ", referenceNo='" + referenceNo + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", receiveQty=" + receiveQty +
                ", receiveTime=" + receiveTime +
                ", status=" + status +
                '}';
    }
}
