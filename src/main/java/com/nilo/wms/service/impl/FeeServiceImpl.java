package com.nilo.wms.service.impl;

import com.alibaba.fastjson.JSON;

import com.nilo.mq.model.NotifyRequest;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.common.util.MailInfo;
import com.nilo.wms.common.util.SendEmailUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dao.flux.WMSFeeDao;
import com.nilo.mq.producer.AbstractMQProducer;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.dto.common.InterfaceConfig;
import com.nilo.wms.dto.fee.Fee;
import com.nilo.wms.dto.fee.FeeDO;
import com.nilo.wms.dto.fee.FeePrice;
import com.nilo.wms.service.FeeService;
import com.nilo.wms.service.config.SystemConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Administrator on 2017/6/9.
 */
@Service
public class FeeServiceImpl implements FeeService {

    private static final Logger logger = LoggerFactory.getLogger(FeeService.class);
    @Autowired
    private WMSFeeDao feeDao;
    @Autowired
    @Qualifier("notifyDataBusProducer")
    private AbstractMQProducer notifyDataBusProducer;

    @Override
    public List<Fee> queryStorageFee(String clientCode, String date) {
        List<Fee> resultList = new ArrayList<>();
        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }
        List<FeeDO> list = feeDao.queryStorage(config.getCustomerCode(), config.getWarehouseCode());

        for (FeeDO d : list) {
            Fee f = new Fee();
            f.setSeller_id(d.getStoreId());
            f.setFactor1(d.getCategories());

            f.setFactor2(getFactor2(clientCode, "inventory", d));
            f.setClass_id(d.getCategories());
            f.setQty(d.getQty());
            f.setSku(d.getSku());
            f.setReceivable_money(getMoney(clientCode, "inventory", d.getCategories(), d.getQty(), false));
            resultList.add(f);
        }

        return resultList;
    }

    @Override
    public List<Fee> queryInboundOrder(String clientCode, String date) {
        List<Fee> resultList = new ArrayList<>();
        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }
        List<FeeDO> list = feeDao.queryInBoundOrderHandler(config.getCustomerCode(), config.getWarehouseCode(), date);
        for (FeeDO o : list) {
            Fee f = new Fee();
            f.setOrder_sn(o.getOrderNo());
            f.setOrder_no(o.getNo());
            f.setClass_id(o.getCategories());
            f.setSeller_id(o.getStoreId());
            f.setSeller_name(o.getStoreDesc());
            f.setFactor1(o.getCategories());
            f.setSku(o.getSku());
            f.setQty(o.getQty());

            f.setFactor2(getFactor2(clientCode, "inbound", o));

            f.setReceivable_money(getMoney(clientCode, "inbound", o.getCategories(), o.getQty(), false));

            resultList.add(f);
        }
        return resultList;
    }

    @Override
    public List<Fee> queryOrderHandlerFee(String clientCode, String date) {
        List<Fee> resultList = new ArrayList<>();

        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }

        List<FeeDO> list = feeDao.queryOrderHandler(config.getCustomerCode(), config.getWarehouseCode(), date);

        Map<String, String> categoriesMap = new HashMap<>();

        for (FeeDO o : list) {
            Fee f = new Fee();
            f.setOrder_sn(o.getOrderNo());
            f.setOrder_no(o.getNo());
            f.setSeller_id(o.getStoreId());
            f.setSeller_name(o.getStoreDesc());
            f.setFactor1(o.getCategories());
            f.setSku(o.getSku());
            f.setQty(o.getQty());

            f.setFactor2(getFactor2(clientCode, "handle", o));
            f.setClass_id(o.getCategories());

            if (StringUtil.isNotEmpty(categoriesMap.get(o.getOrderNo() + o.getCategories() + o.getStoreId()))) {
                f.setReceivable_money(getMoney(clientCode, "handle", o.getCategories(), o.getQty(), true));
            } else {
                f.setReceivable_money(getMoney(clientCode, "handle", o.getCategories(), o.getQty(), false));
            }

            //记录已计算该类别
            categoriesMap.put(o.getOrderNo() + o.getCategories() + o.getStoreId(), o.getCategories());
            resultList.add(f);
        }
        return resultList;
    }

    @Override
    public List<Fee> queryOrderReturnHandlerFee(String clientCode, String date) {
        List<Fee> resultList = new ArrayList<>();
        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }
        List<FeeDO> list = feeDao.queryOrderReturn(config.getCustomerCode(), config.getWarehouseCode(), date);

        Map<String, String> categoriesMap = new HashMap<>();
        for (FeeDO o : list) {
            Fee f = new Fee();
            f.setOrder_sn(o.getOrderNo());
            f.setOrder_no(o.getNo());
            f.setSeller_id(o.getStoreId());
            f.setSeller_name(o.getStoreDesc());
            f.setFactor1(o.getCategories());
            f.setSku(o.getSku());
            f.setQty(o.getQty());

            f.setFactor2(getFactor2(clientCode, "return", o));
            f.setClass_id(o.getCategories());

            if (StringUtil.isNotEmpty(categoriesMap.get(o.getOrderNo() + o.getCategories() + o.getStoreId()))) {
                f.setReceivable_money(getMoney(clientCode, "return", o.getCategories(), o.getQty(), true));
            } else {
                f.setReceivable_money(getMoney(clientCode, "return", o.getCategories(), o.getQty(), false));
            }

            //记录已计算该类别
            categoriesMap.put(o.getOrderNo() + o.getCategories() + o.getStoreId(), o.getCategories());
            resultList.add(f);
        }
        return resultList;
    }

    @Override
    public List<Fee> queryReturnMerchantHandlerFee(String clientCode, String date) {
        List<Fee> resultList = new ArrayList<>();
        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }
        List<FeeDO> list = feeDao.queryReturnMerchant(config.getCustomerCode(), config.getWarehouseCode(), date);
        for (FeeDO o : list) {
            Fee f = new Fee();
            f.setOrder_sn(o.getOrderNo());
            f.setOrder_no(o.getNo());
            f.setSeller_id(o.getStoreId());
            f.setSeller_name(o.getStoreDesc());
            f.setFactor1(o.getCategories());
            f.setQty(o.getQty());
            f.setSku(o.getSku());

            f.setFactor2(getFactor2(clientCode, "return_merchant", o));
            f.setClass_id(o.getCategories());

            f.setReceivable_money(getMoney(clientCode, "return_merchant", o.getCategories(), o.getQty(), false));

            resultList.add(f);
        }
        return resultList;
    }

    @Override
    public void syncToNOS(List<Fee> list, String clientCode, String date, String moneyType) {

        if (list == null || list.size() == 0) return;


        //设置调用api主体信息
        ClientConfig config = SystemConfig.getClientConfig().get(clientCode);
        if (config == null) {
            throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        }
        //设置调用api主体信息
        Principal principal = new Principal();
        principal.setClientCode(clientCode);
        principal.setCustomerId(config.getCustomerCode());
        principal.setWarehouseId(config.getWarehouseCode());
        SessionLocal.setPrincipal(principal);

        InterfaceConfig interfaceConfig = SystemConfig.getInterfaceConfig().get(clientCode).get("wms_fee");


        //写入 nos
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("date", date);
        map.put("type_id", "1");
        map.put("charge_type", "1");
        map.put("money_type", moneyType);
        String data = JSON.toJSONString(map);
        NotifyRequest notify = new NotifyRequest();

        Map<String, String> params = new HashMap<>();
        params.put("method", interfaceConfig.getMethod());
        params.put("sign", createNOSSign(data, config.getClientKey()));
        try {
            params.put("data", URLEncoder.encode(data, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.put("app_key", "wms");
        params.put("country_code", "ke");
        params.put("timestamp", "" + DateUtil.getSysTimeStamp());
        String request_id = UUID.randomUUID().toString();
        params.put("request_id", request_id);
        notify.setParam(params);
        notify.setUrl(interfaceConfig.getUrl());
        try {
            notifyDataBusProducer.sendMessage(notify);
        } catch (Exception e) {
            MailInfo mailInfo = new MailInfo();
            mailInfo.setSubject("WMS Fee Type: " + moneyType + " Date: " + date + " Notify Failed");
            List to = new ArrayList<>();
            to.add("ronny.zeng@kilimall.com");
            mailInfo.setToAddress(to);
            mailInfo.setContent(request_id);
            SendEmailUtil.sendEmail(mailInfo);
            logger.error("WMS Fee send message failed.", e);
        }
    }

    private String createNOSSign(String data, String key) {
        String str = key + data + key;
        return DigestUtils.md5Hex(str).toUpperCase();
    }

    /**
     * @param prefix
     * @param categories
     * @param storage
     * @param isNext     是否是续件
     * @return
     */
    private double getMoney(String clientCode, String prefix, String categories, int storage, boolean isNext) {
        FeePrice fee = SystemConfig.getFeeConfig().get(clientCode).get(prefix + categories);
        if (fee == null) {
            fee = SystemConfig.getFeeConfig().get(clientCode).get(prefix + "other");
        }
        BigDecimal price = null;

        BigDecimal storageBigDecimal = BigDecimal.valueOf(storage);
        if (isNext) {
            price = fee.getNextPrice().multiply(storageBigDecimal);
        } else {
            // 配置信息中续件配置为0则表示不计算续件
            BigDecimal nextStorage = BigDecimal.valueOf(storage - 1);
            price = fee.getNextPrice() == null ? fee.getFirstPrice().multiply(storageBigDecimal) : fee.getFirstPrice().add(fee.getNextPrice().multiply(nextStorage));
        }
        return price.doubleValue();
    }

    private String getFactor2(String clientCode, String prefix, FeeDO h) {
        FeePrice fee = SystemConfig.getFeeConfig().get(clientCode).get(prefix + h.getCategories());
        if (fee == null) {
            h.setCategories("9999");
            fee = SystemConfig.getFeeConfig().get(clientCode).get(prefix + "other");
        }

        return fee.getNextPrice() == null ? "" + fee.getFirstPrice().doubleValue() : "" + fee.getFirstPrice().doubleValue() + "/" + fee.getNextPrice();
    }

}
