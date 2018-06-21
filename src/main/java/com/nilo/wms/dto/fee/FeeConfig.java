package com.nilo.wms.dto.fee;

import com.nilo.wms.common.BaseDo;

/**
 * Created by ronny on 2017/8/30.
 */
public class FeeConfig extends BaseDo<Long> {

    private String clientCode;

    private String feeType;

    private String classType;

    private String classTypeDesc;

    private Double firstPrice;

    private Double secondPrice;

    public String getClassTypeDesc() {
        return classTypeDesc;
    }

    public void setClassTypeDesc(String classTypeDesc) {
        this.classTypeDesc = classTypeDesc;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public Double getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(Double firstPrice) {
        this.firstPrice = firstPrice;
    }

    public Double getSecondPrice() {
        return secondPrice;
    }

    public void setSecondPrice(Double secondPrice) {
        this.secondPrice = secondPrice;
    }
}
