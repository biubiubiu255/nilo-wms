package com.nilo.wms.dto.outbound;

import com.nilo.wms.common.annotation.Excel;

/**
 * User: Alvin
 * Date: 2018/07/16 17:12
 * Just go for it and give it a try
 */
public class ReportOutboundDetail {
    @Excel(name = "OmsOrder", order = 1)
    private String omsOrder;
    @Excel(name = "AddTime", order = 2)
    private String orderIssueTime;
    @Excel(name = "PickTime", order = 3)
    private String orderPickingTime;
    @Excel(name = "CheckTime", order = 4)
    private String orderCheckTime;
    @Excel(name = "ShipTime", order = 5)
    private String orderLoadingTime;
    @Excel(name = "WmsStatus", order = 6)
    private String wmsStatus;

    public String getOmsOrder() {
        return omsOrder;
    }

    public void setOmsOrder(String omsOrder) {
        this.omsOrder = omsOrder;
    }

    public String getOrderIssueTime() {
        return orderIssueTime;
    }

    public void setOrderIssueTime(String orderIssueTime) {
        this.orderIssueTime = orderIssueTime;
    }

    public String getOrderPickingTime() {
        return orderPickingTime;
    }

    public void setOrderPickingTime(String orderPickingTime) {
        this.orderPickingTime = orderPickingTime;
    }

    public String getOrderCheckTime() {
        return orderCheckTime;
    }

    public void setOrderCheckTime(String orderCheckTime) {
        this.orderCheckTime = orderCheckTime;
    }

    public String getOrderLoadingTime() {
        return orderLoadingTime;
    }

    public void setOrderLoadingTime(String orderLoadingTime) {
        this.orderLoadingTime = orderLoadingTime;
    }

    public String getWmsStatus() {
        return wmsStatus;
    }

    public void setWmsStatus(String wmsStatus) {
        this.wmsStatus = wmsStatus;
    }

    @Override
    public String toString() {
        return "ReportOutboundDetail{" +
                "omsOrder='" + omsOrder + '\'' +
                ", orderIssueTime='" + orderIssueTime + '\'' +
                ", orderPickingTime='" + orderPickingTime + '\'' +
                ", orderCheckTime='" + orderCheckTime + '\'' +
                ", orderLoadingTime='" + orderLoadingTime + '\'' +
                ", wmsStatus='" + wmsStatus + '\'' +
                '}';
    }
}
