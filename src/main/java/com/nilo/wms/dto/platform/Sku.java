package com.nilo.wms.dto.platform;

import com.nilo.wms.common.BaseDo;
import com.nilo.wms.dto.common.InterfaceConfig;

public class Sku extends BaseDo<Integer> {

    private String customerCode;

    private String sku;

    private String desc_c;

    private String desc_e;

    private Double price;

    private String freightClass;

    private String image;

    private Integer status;

    private String logisticType;

    private String storeId;

    private String storeName;

    private Integer safeStorage;

    public Integer getSafeStorage() {
        return safeStorage;
    }

    public void setSafeStorage(Integer safeStorage) {
        this.safeStorage = safeStorage;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDesc_c() {
        return desc_c;
    }

    public void setDesc_c(String desc_c) {
        this.desc_c = desc_c;
    }

    public String getDesc_e() {
        return desc_e;
    }

    public void setDesc_e(String desc_e) {
        this.desc_e = desc_e;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getFreightClass() {
        return freightClass;
    }

    public void setFreightClass(String freightClass) {
        this.freightClass = freightClass;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLogisticType() {
        return logisticType;
    }

    public void setLogisticType(String logisticType) {
        this.logisticType = logisticType;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
