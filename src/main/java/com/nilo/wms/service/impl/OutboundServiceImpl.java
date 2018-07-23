package com.nilo.wms.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.flux.FLuxRequest;
import com.nilo.wms.dto.flux.FluxOutbound;
import com.nilo.wms.dto.flux.FluxResponse;
import com.nilo.wms.dto.flux.FluxWeight;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.outbound.OutboundItem;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.dto.platform.outbound.Outbound;
import com.nilo.wms.dto.platform.outbound.OutboundDetail;
import com.nilo.wms.dto.platform.parameter.OutboundParam;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.HttpRequest;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.service.platform.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource(name = "fluxHttpRequest")
    private HttpRequest<FLuxRequest, FluxResponse> fluxHttpRequest;
    @Autowired
    private SystemService systemService;
    @Autowired
    private OutboundDao outboundDao;
    @Autowired
    private OutboundItemDao outboundItemDao;
    @Autowired
    private FluxOutboundDao fluxOutboundDao;
    @Autowired
    private BasicDataService basicDataService;
    @Value("#{configProperties['flux_status']}")
    private String flux_status;

    @Override
    public void createOutBound(OutboundHeader outBound) {

        AssertUtil.isNotNull(outBound, SysErrorCode.REQUEST_IS_NULL);

        AssertUtil.isNotBlank(outBound.getOrderNo(), CheckErrorCode.CLIENT_ORDER_EMPTY);
        AssertUtil.isNotBlank(outBound.getOrderType(), CheckErrorCode.ORDER_TYPE_EMPTY);
        AssertUtil.isNotBlank(outBound.getChannel(), CheckErrorCode.DELIVERY_TYPE_EMPTY);
        AssertUtil.isNotBlank(outBound.getIsCod(), CheckErrorCode.IS_POD_EMPTY);
        AssertUtil.isNotBlank(outBound.getDeliveryNo(), CheckErrorCode.WAYBILL_EMPTY);
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
        if (outboundDO != null) {
            return;
        }

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
        if (outboundDO == null) return;
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
            String requestId = UUID.randomUUID().toString();
            RedisUtil.tryGetDistributedLock(RedisUtil.LOCK_KEY, requestId);
            for (OutboundDetail item : itemList) {
                String key = RedisUtil.getSkuKey(clientCode, item.getSku());
                String sto = RedisUtil.hget(key, RedisUtil.STORAGE);
                int stoInt = sto == null ? 0 : Integer.parseInt(sto) + item.getQty();
                RedisUtil.hset(key, RedisUtil.STORAGE, "" + stoInt);
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

        List<String> waybillList = new ArrayList<>();

        for (Outbound out : outList) {

            if (out.getStatus() == OutBoundStatusEnum.closed.getCode() || out.getStatus() == OutBoundStatusEnum.cancelled.getCode()) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            if (result) {
                out.setStatus(OutBoundStatusEnum.closed.getCode());
                map.put("status", 240);
                waybillList.add(out.getWaybillNum());
            } else {
                out.setStatus(OutBoundStatusEnum.cancelled.getCode());
                map.put("status", 0);
            }
            map.put("client_ordersn", out.getReferenceNo());
            map.put("order_type", out.getOrderType());
            String data = JSON.toJSONString(map);
            systemService.notifyDataBus(data, clientCode, "wms_outbound_notify");
            outboundDao.update(out);
        }

        if (!waybillList.isEmpty()) {
            // 修改DMS重量
            notifyWeight(waybillList);
            // DMS 自动到件
            arriveScan(waybillList);
        }
    }

    @Override
    public String getSubWaybill(String orderNo) {

        String clientCode = SessionLocal.getPrincipal().getClientCode();
        Long serialNum = RedisUtil.increment("old_waybill_num");
        String waybillNum = "KE2" + String.format("%0" + 7 + "d", serialNum);
        Map<String, String> data = new HashMap<>();
        data.put("sub_waybill_number", waybillNum);
        data.put("waybill_number", orderNo);
        systemService.notifyDataBus(JSON.toJSONString(data), clientCode, "get_sub_delivery_no");
        return waybillNum;
    }

    private void notifyWeight(List<String> list) {
        String clientCode = SessionLocal.getPrincipal().getClientCode();

        if (StringUtil.equals(flux_status, "close")) {
            List<FluxWeight> weightList = new ArrayList<>();
            for (String s : list) {
                FluxWeight weight = new FluxWeight();
                weight.setWeight(12.2d);
                weight.setWaybillNum(s);
                weightList.add(weight);
            }
            String updateData = JSON.toJSONString(weightList);
            systemService.notifyDataBus(updateData, clientCode, "update_weight");
        } else {
            List<FluxWeight> weightList = fluxOutboundDao.queryWeight(list);
            String updateData = JSON.toJSONString(weightList);
            systemService.notifyDataBus(updateData, clientCode, "update_weight");
        }
    }

    private void arriveScan(List<String> list) {
        String clientCode = SessionLocal.getPrincipal().getClientCode();
        String updateData = JSON.toJSONString(list);
        systemService.notifyDataBus(updateData, clientCode, "arrive_scan");
    }

    @Override
    public FluxOutbound queryFlux(String orderNo) {

        AssertUtil.isNotBlank(orderNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        Principal principal = SessionLocal.getPrincipal();

        if (StringUtil.equals(flux_status, "close")) {

            Outbound outbound = outboundDao.queryByReferenceNo(principal.getClientCode(), orderNo);
            if (outbound == null) throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
            FluxOutbound order = new FluxOutbound();
            order.setStatus(outbound.getStatus());
            order.setReferenceNo(orderNo);
            order.setWmsOrderNo("Ronny");
            order.setStatusDesc(OutBoundStatusEnum.getEnum(outbound.getStatus()).getDesc_e());
            return order;
        } else {
            FluxOutbound order = fluxOutboundDao.queryByReferenceNo(principal.getCustomerId(), orderNo);
            if (order == null) throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
            order.setStatusDesc(OutBoundStatusEnum.getEnum(order.getStatus()).getDesc_e());
            return order;
        }
    }

    @Override
    public void ship(String referenceNo) {

        String clientCode = SessionLocal.getPrincipal().getClientCode();
        Outbound out = outboundDao.queryByReferenceNo(clientCode, referenceNo);

        Map<String, Object> map = new HashMap<>();
        out.setStatus(OutBoundStatusEnum.closed.getCode());
        map.put("status", 240);
        map.put("client_ordersn", out.getReferenceNo());
        map.put("order_type", out.getOrderType());
        String data = JSON.toJSONString(map);
        systemService.notifyDataBus(data, clientCode, "wms_outbound_notify");

        out.setStatus(OutBoundStatusEnum.closed.getCode());
        outboundDao.update(out);

        // DMS 自动到件
        List<String> list = new ArrayList<>();
        list.add(out.getWaybillNum());
        arriveScan(list);

        // 修改DMS重量
        notifyWeight(list);
    }

    @Override
    public void cancel(String referenceNo) {
        String clientCode = SessionLocal.getPrincipal().getClientCode();
        Outbound out = outboundDao.queryByReferenceNo(clientCode, referenceNo);

        Map<String, Object> map = new HashMap<>();
        out.setStatus(OutBoundStatusEnum.closed.getCode());
        map.put("status", 0);
        map.put("client_ordersn", out.getReferenceNo());
        map.put("order_type", out.getOrderType());
        String data = JSON.toJSONString(map);
        systemService.notifyDataBus(data, clientCode, "wms_outbound_notify");

        out.setStatus(OutBoundStatusEnum.cancelled.getCode());
        outboundDao.update(out);
    }

    @Override
    public PageResult<Outbound> queryBy(OutboundParam param) {
        Long count = outboundDao.queryCountBy(param);
        PageResult<Outbound> pageResult = new PageResult<>();
        if (count == 0) {
            return pageResult;
        }
        List<Outbound> list = outboundDao.queryBy(param);
        pageResult.setData(list);
        return pageResult;
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
