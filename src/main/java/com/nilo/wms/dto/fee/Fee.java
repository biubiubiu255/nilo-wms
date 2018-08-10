package com.nilo.wms.dto.fee;

import com.nilo.wms.common.annotation.Excel;

/**
 * Created by Administrator on 2017/6/9.
 */
public class Fee {

    private String charge_type; //

    @Excel(name = "MoneyType", order = 2)
    private String money_type;  //

    private String factor1;

    private String factor2;

    private String factor3;

    private String factor4;

    private String factor5;

    private String rate;

    private String order_no;

    @Excel(name = "OrderSn", order = 3)
    private String order_sn;//

    @Excel(name = "StoreId", order = 4)
    private String store_id;//

    @Excel(name = "StoreName", order = 5)
    private String store_name;//

    @Excel(name = "ReceivableMoney", order = 6)
    private double receivable_money;//

    private double real_money;

    private double payable_money;

    private double paid_money;

    private String pay_direct;

    private String pay_type;

    private String biz_status;

    private String settlement_status;

    private String is_paid;

    private String waybill_number;

    private double weight;

    private String city;

    private String class_id;

    private String sku;

    private int qty;

    @Excel(name = "CreatedTime", order = 7)
    private String createdTimeDesc;

    public String getCharge_type() {
        return charge_type;
    }

    public void setCharge_type(String charge_type) {
        this.charge_type = charge_type;
    }

    public String getMoney_type() {
        return money_type;
    }

    public void setMoney_type(String money_type) {
        this.money_type = money_type;
    }

    public String getFactor1() {
        return factor1;
    }

    public void setFactor1(String factor1) {
        this.factor1 = factor1;
    }

    public String getFactor2() {
        return factor2;
    }

    public void setFactor2(String factor2) {
        this.factor2 = factor2;
    }

    public String getFactor3() {
        return factor3;
    }

    public void setFactor3(String factor3) {
        this.factor3 = factor3;
    }

    public String getFactor4() {
        return factor4;
    }

    public void setFactor4(String factor4) {
        this.factor4 = factor4;
    }

    public String getFactor5() {
        return factor5;
    }

    public void setFactor5(String factor5) {
        this.factor5 = factor5;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getOrder_sn() {
        return order_sn;
    }

    public void setOrder_sn(String order_sn) {
        this.order_sn = order_sn;
    }

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public double getReceivable_money() {
        return receivable_money;
    }

    public void setReceivable_money(double receivable_money) {
        this.receivable_money = receivable_money;
    }

    public double getReal_money() {
        return real_money;
    }

    public void setReal_money(double real_money) {
        this.real_money = real_money;
    }

    public double getPayable_money() {
        return payable_money;
    }

    public void setPayable_money(double payable_money) {
        this.payable_money = payable_money;
    }

    public double getPaid_money() {
        return paid_money;
    }

    public void setPaid_money(double paid_money) {
        this.paid_money = paid_money;
    }

    public String getPay_direct() {
        return pay_direct;
    }

    public void setPay_direct(String pay_direct) {
        this.pay_direct = pay_direct;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getBiz_status() {
        return biz_status;
    }

    public void setBiz_status(String biz_status) {
        this.biz_status = biz_status;
    }

    public String getSettlement_status() {
        return settlement_status;
    }

    public void setSettlement_status(String settlement_status) {
        this.settlement_status = settlement_status;
    }

    public String getIs_paid() {
        return is_paid;
    }

    public void setIs_paid(String is_paid) {
        this.is_paid = is_paid;
    }

    public String getWaybill_number() {
        return waybill_number;
    }

    public void setWaybill_number(String waybill_number) {
        this.waybill_number = waybill_number;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
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

    public String getCreatedTimeDesc() {
        return createdTimeDesc;
    }

    public void setCreatedTimeDesc(String createdTimeDesc) {
        this.createdTimeDesc = createdTimeDesc;
    }
}
