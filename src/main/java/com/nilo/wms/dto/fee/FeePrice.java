package com.nilo.wms.dto.fee;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/6/9.
 */
public class FeePrice {

    private Double firstPrice;

    private Double nextPrice;

    public Double getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(Double firstPrice) {
        this.firstPrice = firstPrice;
    }

    public Double getNextPrice() {
        return nextPrice;
    }

    public void setNextPrice(Double nextPrice) {
        this.nextPrice = nextPrice;
    }
}
