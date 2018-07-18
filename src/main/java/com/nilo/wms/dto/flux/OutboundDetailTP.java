/**
 * Kilimall Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.dto.flux;

import com.nilo.wms.common.annotation.Excel;

public class OutboundDetailTP {

    @Excel(name = "Sku", order = 1)
    private String  sku;
    @Excel(name = "Sku Desc", order = 2)
    private String  skuDesc;
    @Excel(name = "Location", order = 3)
    private String  locationId;
    @Excel(name = "Trace", order = 4)
    private String traceId;
    @Excel(name = "Qty", order = 5)
    private Integer qty;

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSkuDesc() {
        return skuDesc;
    }

    public void setSkuDesc(String skuDesc) {
        this.skuDesc = skuDesc;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
