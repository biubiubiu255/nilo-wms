package com.nilo.wms.service;

import com.nilo.wms.dto.fee.Fee;

import java.util.List;

/**
 * Created by Administrator on 2017/6/9.
 */
public interface FeeService {

    Integer STATUS = 1;

    List<Fee> queryStorageFee(String clientCode);  //暂停

    List<Fee> queryInboundOrder(String clientCode, String fromDate, String toDate);  //

    List<Fee> queryOrderHandlerFee(String clientCode, String fromDate, String toDate);//

    List<Fee> queryOrderReturnHandlerFee(String clientCode, String fromDate, String toDate);//

    List<Fee> queryReturnMerchantHandlerFee(String clientCode, String fromDate, String toDate);//

    void syncToNOS(List<Fee> list, String clientCode, String date, String moneyType);
}
