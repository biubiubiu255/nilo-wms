package com.nilo.wms.dto.inbound;

import com.nilo.wms.common.annotation.Excel;
import com.nilo.wms.common.enums.InboundStatusEnum;

/**
 * User: Alvin
 * Date: 2018/07/16 17:12
 * Just go for it and give it a try
 */
public class ReportInboundDetail {
    @Excel(name = "OrderNo", order = 1)
    private String  orderNo;
    @Excel(name = "InboundStatus", order = 2)
    private Integer inboundStatus;
    @Excel(name = "InboundType", order = 3)
    private String  inboundType;
    @Excel(name = "InboundTime", order = 4)
    private String  inboundTime;
    @Excel(name = "Linestatus", order = 5)
    private Integer linestatus;

    @Excel(name = "SKU", order = 6)
    private Integer sku;
    @Excel(name = "ReceivedTime", order = 7)
    private String  receivedTime;
    @Excel(name = "ExpectedQty", order = 8)
    private Integer expectedQty;
    @Excel(name = "ReceivedQty", order = 9)
    private Integer receivedQty;

    @Excel(name = "PutawaySku", order = 10)
    private Integer putawaySku;
    @Excel(name = "PutawayQty", order = 11)
    private Integer putawayQty;
    @Excel(name = "PutawayTime", order = 12)
    private String  putawayTime;


    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getInboundStatusDesc() {
        return InboundStatusEnum.getEnum(inboundStatus).getDesc_e();
    }

    public Integer getInboundStatus() {
        return inboundStatus;
    }

    public void setInboundStatus(Integer inboundStatus) {
        this.inboundStatus = inboundStatus;
    }

    public String getInboundType() {
        return inboundType;
    }

    public void setInboundType(String inboundType) {
        this.inboundType = inboundType;
    }

    public String getInboundTime() {
        return inboundTime;
    }

    public void setInboundTime(String inboundTime) {
        this.inboundTime = inboundTime;
    }

    public String getLinestatusDesc() {
        return InboundStatusEnum.getEnum(linestatus).getDesc_e();
    }

    public Integer getLinestatus() {
        return linestatus;
    }

    public void setLinestatus(Integer linestatus) {
        this.linestatus = linestatus;
    }

    public Integer getSku() {
        return sku;
    }

    public void setSku(Integer sku) {
        this.sku = sku;
    }

    public Integer getExpectedQty() {
        return expectedQty;
    }

    public void setExpectedQty(Integer expectedQty) {
        this.expectedQty = expectedQty;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Integer getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(Integer receivedQty) {
        this.receivedQty = receivedQty;
    }

    public Integer getPutawaySku() {
        return putawaySku;
    }

    public void setPutawaySku(Integer putawaySku) {
        this.putawaySku = putawaySku;
    }

    public Integer getPutawayQty() {
        return putawayQty;
    }

    public void setPutawayQty(Integer putawayQty) {
        this.putawayQty = putawayQty;
    }

    public String getPutawayTime() {
        return putawayTime;
    }

    public void setPutawayTime(String putawayTime) {
        this.putawayTime = putawayTime;
    }

}
