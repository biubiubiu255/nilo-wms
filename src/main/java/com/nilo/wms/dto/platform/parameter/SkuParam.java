package com.nilo.wms.dto.platform.parameter;

import com.nilo.wms.dto.common.Page;

import java.util.List;

/**
 * Created by admin on 2018/4/26.
 */
public class SkuParam extends Page {

    private String desc;

    private List<String> storeList;

    private List<String> skuList;

    public List<String> getStoreList() {
        return storeList;
    }

    public void setStoreList(List<String> storeList) {
        this.storeList = storeList;
    }

    public List<String> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<String> skuList) {
        this.skuList = skuList;
    }

    private String customerCode;

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
