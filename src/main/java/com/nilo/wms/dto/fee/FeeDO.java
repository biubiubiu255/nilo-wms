package com.nilo.wms.dto.fee;

/**
 * Created by Administrator on 2017/6/9.
 */
public class FeeDO {

    private String no;

    private String orderNo;

    private String orderType;

    private String categories;

    private String storeId;

    private String storeDesc;

    private String sku;

    private int qty;

    private String createdTimeDesc;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public String getCreatedTimeDesc() {
        return createdTimeDesc;
    }

    public void setCreatedTimeDesc(String createdTimeDesc) {
        this.createdTimeDesc = createdTimeDesc;
    }
}
