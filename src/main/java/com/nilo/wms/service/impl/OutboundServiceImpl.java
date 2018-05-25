package com.nilo.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.nilo.mq.model.NotifyRequest;
import com.nilo.mq.producer.AbstractMQProducer;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.enums.OutBoundStatusEnum;
import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.common.util.XmlUtil;
import com.nilo.wms.dao.flux.FluxOutboundDao;
import com.nilo.wms.dao.platform.OutboundDao;
import com.nilo.wms.dao.platform.OutboundItemDao;
import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.dto.common.InterfaceConfig;
import com.nilo.wms.dto.flux.FLuxRequest;
import com.nilo.wms.dto.flux.FluxOutbound;
import com.nilo.wms.dto.flux.FluxResponse;
import com.nilo.wms.dto.flux.FluxWeight;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.outbound.OutboundItem;
import com.nilo.wms.dto.platform.outbound.Outbound;
import com.nilo.wms.dto.platform.outbound.OutboundDetail;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.HttpRequest;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.service.config.SystemConfig;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.web.model.ResultMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created by admin on 2018/3/19.
 */
@Service
public class OutboundServiceImpl implements OutboundService {
    private static final Logger logger = LoggerFactory.getLogger(OutboundServiceImpl.class);

    @Resource(name = "fluxHttpRequest")
    private HttpRequest<FLuxRequest, FluxResponse> fluxHttpRequest;
    @Autowired
    @Qualifier("notifyDataBusProducer")
    private AbstractMQProducer notifyDataBusProducer;
    @Autowired
    private OutboundDao outboundDao;
    @Autowired
    private OutboundItemDao outboundItemDao;
    @Autowired
    private FluxOutboundDao fluxOutboundDao;
    @Autowired
    private BasicDataService basicDataService;

    @Override
    public void createOutBound(OutboundHeader outBound) {

        AssertUtil.isNotNull(outBound, SysErrorCode.REQUEST_IS_NULL);

        AssertUtil.isNotBlank(outBound.getOrderNo(), CheckErrorCode.CLIENT_ORDER_EMPTY);
        AssertUtil.isNotBlank(outBound.getOrderType(), CheckErrorCode.ORDER_TYPE_EMPTY);
        AssertUtil.isNotBlank(outBound.getChannel(), CheckErrorCode.DELIVERY_TYPE_EMPTY);
        AssertUtil.isNotBlank(outBound.getIsCod(), CheckErrorCode.IS_POD_EMPTY);
        AssertUtil.isNotBlank(outBound.getDeliveryNo(), CheckErrorCode.WAYBILL_EMPTY);
        AssertUtil.isFalse(outBound.getOrderAmount() == 0, CheckErrorCode.ORDER_AMOUNT_EMPTY);
        AssertUtil.isNotNull(outBound.getItemList(), CheckErrorCode.ITEM_EMPTY);
        AssertUtil.isNotNull(outBound.getReceiverInfo(), CheckErrorCode.RECEIVER_INFO_EMPTY);
        AssertUtil.isNotBlank(outBound.getReceiverInfo().getReceiverAddress(), CheckErrorCode.RECEIVER_ADDRESS_EMPTY);
        AssertUtil.isNotBlank(outBound.getReceiverInfo().getReceiverName(), CheckErrorCode.RECEIVER_NAME_EMPTY);
        AssertUtil.isNotBlank(outBound.getReceiverInfo().getReceiverPhone(), CheckErrorCode.RECEIVER_PHONE_EMPTY);

        if (StringUtil.isEmpty(outBound.getOrderTime())) {
            outBound.setOrderTime(DateUtil.getSysTimeStamp());
        }

        //设置仓库信息
        Principal principal = SessionLocal.getPrincipal();
        String clientCode = principal.getClientCode();

        Outbound outboundDO = outboundDao.queryByReferenceNo(clientCode, outBound.getOrderNo());
        if (outboundDO != null) return;

        // 判断订单号是否锁定库存过
        String orderNoKey = RedisUtil.getLockOrderKey(clientCode, outBound.getOrderNo());
        boolean keyExist = RedisUtil.hasKey(orderNoKey);
        //锁定库存记录不存在
        if (!keyExist) {
            List<StorageInfo> lockResp = basicDataService.lockStorage(outBound);
            if (lockResp != null) {
                throw new WMSException(BizErrorCode.STORAGE_NOT_ENOUGH);
            }
        }

        outBound.setCustomerId(principal.getCustomerId());
        outBound.setWarehouseId(principal.getWarehouseId());

        int lineNo = 0;
        for (OutboundItem item : outBound.getItemList()) {
            item.setCustomerId(principal.getCustomerId());
            item.setLineNo(lineNo + 1);
        }

        //构建flux请求对象
        FLuxRequest request = new FLuxRequest();
        outBound.setReceiverAddress(outBound.getReceiverInfo().getReceiverAddress());
        outBound.setReceiverCity(outBound.getReceiverInfo().getReceiverCity());
        outBound.setReceiverName(outBound.getReceiverInfo().getReceiverName());
        outBound.setReceiverPhone(outBound.getReceiverInfo().getReceiverPhone());

        String xmlData = XmlUtil.BeanToXML(outBound);
        //添加xml数据前缀
        xmlData = "<xmldata>" + xmlData + "</xmldata>";
        request.setData(xmlData);

        switch (outBound.getOrderType()) {
            case "SO":
            case "SELL": {
                request.setMessageid("SO");
                request.setMethod("putSOData");
                break;
            }
            case "WX":
            case "CT":
            case "PK":
            case "HH":
            case "BF": {
                request.setMessageid("CGSO");
                request.setMethod("putCGSOData");
                break;
            }
            default:
                throw new WMSException(BizErrorCode.ORDER_TYPE_NOT_EXIST);
        }

        FluxResponse response = fluxHttpRequest.doRequest(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getReturnDesc());
        }

        //记录出库单信息
        recordOutbound(outBound);

        //下单成功扣减库存
        basicDataService.successStorage(outBound);
    }

    @Override
    public void cancelOutBound(String orderNo) {

        AssertUtil.isNotBlank(orderNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        Principal principal = SessionLocal.getPrincipal();
        String clientCode = principal.getClientCode();
        Outbound outboundDO = outboundDao.queryByReferenceNo(clientCode, orderNo);
        if (outboundDO == null) throw new WMSException(BizErrorCode.NOT_EXIST, orderNo);
        if (outboundDO.getStatus() == OutBoundStatusEnum.cancelled.getCode()) return;

        OutBoundSimpleBean cancelOrder = new OutBoundSimpleBean();
        cancelOrder.setOrderNo(orderNo);
        cancelOrder.setCustomerID(principal.getCustomerId());
        cancelOrder.setWarehouseID(principal.getWarehouseId());
        cancelOrder.setOrderType(outboundDO.getOrderType());

        //构建flux请求对象
        FLuxRequest request = new FLuxRequest();
        String xmlData = XmlUtil.BeanToXML(cancelOrder);
        //添加xml数据前缀
        xmlData = "<xmldata><data>" + xmlData + "</data></xmldata>";
        request.setData(xmlData);
        request.setMessageid("SOC");
        request.setMethod("cancelSOData");

        //调用flux 请求
        FluxResponse response = fluxHttpRequest.doRequest(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getReturnDesc());
        }

        Outbound update = new Outbound();
        update.setClientCode(clientCode);
        update.setReferenceNo(orderNo);
        update.setStatus(OutBoundStatusEnum.cancelled.getCode());
        outboundDao.update(update);

        // 增加库存
        List<OutboundDetail> itemList = outboundItemDao.queryByReferenceNo(principal.getClientCode(), orderNo);
        if (itemList != null) {
            //获取redis锁
            Jedis jedis = RedisUtil.getResource();
            String requestId = UUID.randomUUID().toString();
            RedisUtil.tryGetDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);
            for (OutboundDetail item : itemList) {
                String key = RedisUtil.getSkuKey(clientCode, item.getSku());
                String sto = jedis.hget(key, RedisUtil.STORAGE);
                int stoInt = sto == null ? 0 : Integer.parseInt(sto) + item.getQty();
                jedis.hset(key, RedisUtil.STORAGE, "" + stoInt);
            }
        }
    }

    @Override
    public void confirmSO(List<String> list, boolean result) {

        if (list == null || list.size() == 0) return;

        String clientCode = SessionLocal.getPrincipal().getClientCode();
        List<Outbound> outList = outboundDao.queryByList(clientCode, list);
        if (outList == null || outList.size() == 0) {
            throw new WMSException(BizErrorCode.OUTBOUND_NOT_EXIST);
        }
        ClientConfig clientConfig = SystemConfig.getClientConfig().get(clientCode);
        InterfaceConfig interfaceConfig = SystemConfig.getInterfaceConfig().get(clientCode).get("wms_outbound_notify");

        for (Outbound out : outList) {
            Map<String, Object> map = new HashMap<>();
            if (result) {
                map.put("status", 99);
            } else {
                map.put("status", 90);
            }
            map.put("client_ordersn", out.getReferenceNo());
            map.put("order_type", out.getOrderType());

            String data = JSON.toJSONString(map);

            Map<String, String> params = new HashMap<>();
            params.put("method", interfaceConfig.getMethod());
            params.put("sign", createNOSSign(data, clientConfig.getClientKey()));
            params.put("data", data);
            params.put("app_key", "wms");
            params.put("country_code", "ke");
            params.put("request_id", UUID.randomUUID().toString());
            params.put("timestamp", "" + DateUtil.getSysTimeStamp());

            // 通知状态变更
            NotifyRequest notify = new NotifyRequest();
            notify.setParam(params);
            notify.setUrl(interfaceConfig.getUrl());
            try {
                notifyDataBusProducer.sendMessage(notify);
            } catch (Exception e) {
                logger.error("confirmSO send message failed.", e);
            }
        }

        // 修改DMS重量
        List<String> waybillList = new ArrayList<>();
        for (Outbound o : outList) {
            waybillList.add(o.getWaybillNum());
        }
        InterfaceConfig config = SystemConfig.getInterfaceConfig().get(clientCode).get("update_weight");
        List<FluxWeight> weightList = fluxOutboundDao.queryWeight(waybillList);
        String updateData = JSON.toJSONString(weightList);
        Map<String, String> paramsUpdate = new HashMap<>();
        paramsUpdate.put("method", config.getMethod());
        paramsUpdate.put("sign", createNOSSign(updateData, clientConfig.getClientKey()));
        paramsUpdate.put("data", updateData);
        paramsUpdate.put("app_key", "kiliboss");
        paramsUpdate.put("request_id", UUID.randomUUID().toString());
        paramsUpdate.put("timestamp", "" + DateUtil.getSysTimeStamp());
    }

    @Override
    public FluxOutbound queryFlux(String orderNo) {

        AssertUtil.isNotBlank(orderNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        Principal principal = SessionLocal.getPrincipal();

        FluxOutbound order = fluxOutboundDao.queryByReferenceNo(principal.getCustomerId(), orderNo);
        if (order == null) throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
        order.setStatusDesc(OutBoundStatusEnum.getEnum(order.getStatus()).getDesc_e());
        return order;
    }

    private String createNOSSign(String data, String key) {
        String str = key + data + key;
        return DigestUtils.md5Hex(str).toUpperCase();
    }

    @XmlRootElement(name = "ordernos")
    private static class OutBoundSimpleBean {
        private String orderNo;

        private String orderType;

        private String customerId;

        private String warehouseId;

        private String reason;

        @XmlElement(name = "OrderNo")
        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        @XmlElement(name = "OrderType")
        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getCustomerID() {
            return customerId;
        }

        @XmlElement(name = "CustomerID")
        public void setCustomerID(String customerID) {
            customerId = customerID;
        }

        public String getWarehouseID() {
            return warehouseId;
        }

        @XmlElement(name = "WarehouseID")
        public void setWarehouseID(String warehouseID) {
            warehouseId = warehouseID;
        }

        public String getReason() {
            return reason;
        }

        @XmlElement(name = "Reason")
        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    @Transactional
    void recordOutbound(OutboundHeader outBound) {

        //保存
        Outbound insert = new Outbound();
        insert.setClientCode(SessionLocal.getPrincipal().getClientCode());
        insert.setReferenceNo(outBound.getOrderNo());
        insert.setOrderType(outBound.getOrderType());
        insert.setCustomerCode(outBound.getCustomerId());
        insert.setWarehouseCode(outBound.getWarehouseId());
        insert.setStatus(OutBoundStatusEnum.create.getCode());
        insert.setWaybillNum(outBound.getDeliveryNo());
        outboundDao.insert(insert);
        Principal principal = SessionLocal.getPrincipal();

        List<OutboundDetail> list = new ArrayList<>();
        for (OutboundItem item : outBound.getItemList()) {
            OutboundDetail itemDO = new OutboundDetail();
            itemDO.setClientCode(principal.getClientCode());
            itemDO.setSku(item.getSku());
            itemDO.setQty(item.getQty());
            itemDO.setReferenceNo(outBound.getOrderNo());
            list.add(itemDO);
        }

        outboundItemDao.insertBatch(list);
    }
}
