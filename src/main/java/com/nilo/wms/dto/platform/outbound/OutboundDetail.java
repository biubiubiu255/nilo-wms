package com.nilo.wms.dto.platform.outbound;


import com.nilo.wms.common.BaseDo;

/**
 * Created by ronny on 2017/8/30.
 */
public class OutboundDetail extends BaseDo<Long> {

    private String clientCode;
    private String referenceNo;
    private String sku;
    private Integer qty;

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
}
