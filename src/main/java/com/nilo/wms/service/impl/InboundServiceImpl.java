package com.nilo.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.enums.InboundStatusEnum;
import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.*;
import com.nilo.wms.dao.flux.FluxInboundDao;
import com.nilo.wms.dao.flux.FluxInventoryDao;
import com.nilo.wms.dao.flux.WMSInboundDetailDao;
import com.nilo.wms.dao.platform.InboundDao;
import com.nilo.wms.dao.platform.InboundDetailsDao;
import com.nilo.wms.dto.SkuInfo;
import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.flux.FLuxRequest;
import com.nilo.wms.dto.flux.FluxInbound;
import com.nilo.wms.dto.flux.FluxInboundDetails;
import com.nilo.wms.dto.flux.FluxResponse;
import com.nilo.wms.dto.inbound.InboundDetailParam;
import com.nilo.wms.dto.inbound.ReportInboundDetail;
import com.nilo.wms.dto.outbound.OutboundDetailParam;
import com.nilo.wms.dto.outbound.ReportOutboundDetail;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.dto.platform.inbound.InboundDetail;
import com.nilo.wms.dto.inbound.InboundHeader;
import com.nilo.wms.dto.inbound.InboundItem;
import com.nilo.wms.dto.outbound.OutboundItem;
import com.nilo.wms.dto.platform.outbound.OutboundDetail;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.dto.platform.parameter.StorageParam;
import com.nilo.wms.dto.platform.system.Permission;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.FeeService;
import com.nilo.wms.service.HttpRequest;
import com.nilo.wms.service.InboundService;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.service.platform.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by admin on 2018/3/19.
 */
@Service
public class InboundServiceImpl implements InboundService {

    @Resource(name = "fluxHttpRequest")
    private HttpRequest<FLuxRequest, FluxResponse> fluxHttpRequest;
    @Autowired
    private InboundDetailsDao inboundDetailsDao;
    @Autowired
    private InboundDao inboundDao;
    @Autowired
    private FluxInventoryDao skuDao;
    @Autowired
    private BasicDataService basicDataService;
    @Autowired
    private FluxInboundDao fluxInboundDao;
    @Autowired
    private SystemService systemService;
    @Autowired
    private WMSInboundDetailDao wmsInboundDetailDao;

    @Value("#{configProperties['flux_status']}")
    private String flux_status;
    @Value("#{configProperties['sku_url']}")
    private String sku_url;

    @Override
    public void createInBound(InboundHeader inbound) {
        AssertUtil.isNotNull(inbound, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotBlank(inbound.getReferenceNo(), CheckErrorCode.CLIENT_ORDER_EMPTY);
        AssertUtil.isNotBlank(inbound.getAsnType(), CheckErrorCode.ORDER_TYPE_EMPTY);
        AssertUtil.isNotNull(inbound.getItemList(), CheckErrorCode.ITEM_EMPTY);

        if (inbound.getOrderTime() == null) {
            inbound.setOrderTime(DateUtil.getSysTimeStamp());
        }

        //保存
        Principal principal = SessionLocal.getPrincipal();
        String clientCode = principal.getClientCode();
        inbound.setCustomerId(principal.getCustomerId());
        inbound.setWarehouseId(principal.getWarehouseId());
        int lineNo = 0;
        for (InboundItem item : inbound.getItemList()) {
            item.setCustomerId(principal.getCustomerId());
            item.setLineNo(lineNo + 1);
        }

        Inbound inboundDO = inboundDao.queryByReferenceNo(clientCode, inbound.getReferenceNo());
        if (inboundDO != null) return;

        //获取sku信息
        updateSkuInfo(inbound.getItemList());

        //构建flux请求对象
        FLuxRequest request = new FLuxRequest();

        String xmlData = XmlUtil.BeanToXML(inbound);
        //添加xml数据前缀
        xmlData = "<xmldata>" + xmlData + "</xmldata>";
        request.setData(xmlData);

        switch (inbound.getAsnType()) {
            case "PO": {
                request.setMessageid("ASN");
                request.setMethod("putASNData");
                break;
            }
            case "HH":
            case "TH":
            case "JS": {
                request.setMessageid("TRASN");
                request.setMethod("putTRASNData");
                break;
            }
            default:
                throw new WMSException(BizErrorCode.ORDER_TYPE_NOT_EXIST);
        }

        FluxResponse response = fluxHttpRequest.doRequest(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getReturnDesc());
        }

        //保存信息
        recordInbound(inbound);
    }

    private void updateSkuInfo(List<InboundItem> itemList) {
        //下发sku资料
        List<SkuInfo> list = new ArrayList<>();
        for (InboundItem item : itemList) {
            String skuInfoString = HttpUtil.get(sku_url + item.getSku());
            SkuInfo skuInfo = JSON.parseObject(skuInfoString, SkuInfo.class);
            skuInfo.setCustomerId("KILIMALL");
            skuInfo.setSku(item.getSku());
            skuInfo.setPrice("0");
            list.add(skuInfo);
        }
        basicDataService.updateSku(list);
    }

    @Transactional
    void recordInbound(InboundHeader inbound) {
        Principal principal = SessionLocal.getPrincipal();
        String clientCode = principal.getClientCode();

        //保存记录
        Inbound insert = new Inbound();
        insert.setClientCode(clientCode);
        insert.setReferenceNo(inbound.getReferenceNo());
        insert.setReferenceNo2(inbound.getReferenceNo2());
        insert.setCustomerCode(inbound.getCustomerId());
        insert.setWarehouseCode(inbound.getWarehouseId());
        insert.setStatus(InboundStatusEnum.create.getCode());
        insert.setAsnType(inbound.getAsnType());
        inboundDao.insert(insert);


        List<InboundDetail> list = new ArrayList<>();
        for (InboundItem item : inbound.getItemList()) {
            InboundDetail itemDO = new InboundDetail();
            itemDO.setClientCode(principal.getClientCode());
            itemDO.setSku(item.getSku());
            itemDO.setQty(item.getQty());
            itemDO.setReferenceNo(inbound.getReferenceNo());
            list.add(itemDO);
        }

        inboundDetailsDao.insertBatch(list);
    }


    @Override
    public void cancelInBound(String referenceNo) {

        AssertUtil.isNotBlank(referenceNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        String clientCode = SessionLocal.getPrincipal().getClientCode();

        Inbound inboundDO = inboundDao.queryByReferenceNo(clientCode, referenceNo);
        if (inboundDO == null) throw new WMSException(BizErrorCode.NOT_EXIST, referenceNo);
        if (inboundDO.getStatus() == InboundStatusEnum.cancelled.getCode()) return;

        // 通知flux
        FLuxRequest request = new FLuxRequest();
        String xmlData = "<xmldata><data><ordernos><OrderNo>" + referenceNo + "</OrderNo><OrderType>" + inboundDO.getAsnType() + "</OrderType><CustomerID>" + inboundDO.getCustomerCode() + "</CustomerID><WarehouseID>" + inboundDO.getWarehouseCode() + "</WarehouseID></ordernos></data></xmldata>";
        request.setData(xmlData);
        request.setMessageid("ASNC");
        request.setMethod("cancelASNData");

        FluxResponse response = fluxHttpRequest.doRequest(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getReturnDesc());
        }
        //修改状态
        Inbound update = new Inbound();
        update.setClientCode(clientCode);
        update.setReferenceNo(referenceNo);
        update.setStatus(InboundStatusEnum.cancelled.getCode());
        inboundDao.update(update);
    }

    @Override
    public void confirmASN(List<InboundHeader> list) {

        if (list == null || list.size() == 0) {
            return;
        }
        String clientCode = SessionLocal.getPrincipal().getClientCode();

        //查询是否已经通知过
        Iterator<InboundHeader> iterator = list.iterator();
        while (iterator.hasNext()) {
            InboundHeader in = iterator.next();
            Inbound inboundDO = inboundDao.queryByReferenceNo(clientCode, in.getReferenceNo());
            if (inboundDO == null || inboundDO.getStatus() == InboundStatusEnum.closed.getCode()) {
                iterator.remove();
            } else {
                in.setAsnType(inboundDO.getAsnType());
                in.setSupplierId(inboundDO.getSupplierId());
                in.setSupplierName(inboundDO.getSupplierName());
            }
        }

        if (list.size() == 0) return;


        for (InboundHeader in : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 99);
            map.put("client_ordersn", in.getReferenceNo());
            map.put("order_type", in.getAsnType());
            map.put("order_items_list", in.getItemList());
            String data = JSON.toJSONString(map);
            systemService.notifyDataBus(data, clientCode, "wms_inbound_notify");
        }

        //更新inbound状态
        List<String> skuList = new ArrayList<>();
        for (InboundHeader in : list) {
            Inbound update = new Inbound();
            update.setReferenceNo(in.getReferenceNo());
            update.setClientCode(clientCode);
            update.setStatus(InboundStatusEnum.closed.getCode());
            inboundDao.update(update);
            for (InboundItem item : in.getItemList()) {
                skuList.add(item.getSku());
            }
        }

        // 查询sku 仓库实际库存
        StorageParam param = new StorageParam();
        param.setSku(skuList);
        param.setPage(1);
        param.setLimit(100);
        param.setWarehouseId(SessionLocal.getPrincipal().getWarehouseId());
        param.setCustomerId(SessionLocal.getPrincipal().getCustomerId());
        List<StorageInfo> storageList = skuDao.queryBy(param);

        //获取redis锁
        String requestId = UUID.randomUUID().toString();
        RedisUtil.tryGetDistributedLock(RedisUtil.LOCK_KEY, requestId);
        //更新库存
        for (StorageInfo i : storageList) {
            String key = RedisUtil.getSkuKey(clientCode, i.getSku());
            String lockSto = RedisUtil.hget(key, RedisUtil.LOCK_STORAGE);
            i.setLockStorage(lockSto == null ? 0 : Integer.parseInt(lockSto));
            RedisUtil.hset(key, RedisUtil.STORAGE, "" + i.getStorage());
        }
        RedisUtil.releaseDistributedLock(RedisUtil.LOCK_KEY, requestId);

        basicDataService.storageChangeNotify(storageList);
    }

    @Override
    public FluxInbound queryFlux(String referenceNo) {

        AssertUtil.isNotBlank(referenceNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        Principal principal = SessionLocal.getPrincipal();

        if (StringUtil.equals("close", flux_status)) {
            Inbound i = inboundDao.queryByReferenceNo(principal.getClientCode(), referenceNo);
            if (i == null) {
                throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
            }
            FluxInbound inbound = new FluxInbound();
            inbound.setStatus(i.getStatus());
            inbound.setReferenceNo(referenceNo);
            return inbound;
        }
        FluxInbound inbound = fluxInboundDao.queryByReferenceNo(principal.getCustomerId(), referenceNo);
        if (inbound == null) {
            throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
        }
        List<FluxInboundDetails> list = fluxInboundDao.queryDetailsByAsnNo(principal.getCustomerId(), inbound.getWmsAsnNo());
        inbound.setList(list);
        return inbound;
    }


    @Override
    public void inboundScan() {

        /*List<InboundDetail> inboundDetails = inboundDao.queryNotComplete();  //获取wms未完成的详细列表
        if (inboundDetails.isEmpty()) {
            return;
        }
        Set<String> referenceNos = new HashSet<String>();
        for (InboundDetail e : inboundDetails) {                              //抽取wsm所有未完成的订单号Set集合
            referenceNos.add(e.getReferenceNo());
        }
        List<FluxInboundDetails> fluxInboundDetails1 = fluxInboundDao.queryNotCompleteR(referenceNos);   //获取Fl未完成的详细列表
        //比对数据获取完毕

        //比对详细、并修改wms状态
        for (InboundDetail e : inboundDetails) {
            for (FluxInboundDetails f : fluxInboundDetails1) {
                if (StringUtil.equals(e.getReferenceNo(), f.getReferenceNo()) && StringUtil.equals(e.getSku(), f.getSku()) && (!e.getStatus().equals(f.getStatus()) || !e.getReceiveQty().equals(f.getReceivedQty()))) {
                    e.setStatus(Integer.valueOf(f.getStatus()).shortValue());
                    e.setReceiveQty(f.getReceivedQty());
                    if (e.getStatus().equals(InboundStatusEnum.closed.getCode())) {
                        e.setReceiveTime(DateUtil.getSysTimeStamp().intValue());
                    }
                    //System.out.println("正在修改详细 = " + e);
                    //inboundDao.updateDetail(e);
                    break;
                }
            }
        }

        //比对入库单，并修改
        Set<String> fluxRerenceNos = new HashSet<String>();
        for (FluxInboundDetails f : fluxInboundDetails1) {
            fluxRerenceNos.add(f.getReferenceNo());
        }
        for (String e : referenceNos) {
            if (!fluxRerenceNos.contains(e)) {
                Inbound inbound = new Inbound();
                inbound.setReferenceNo(e);
                inbound.setClientCode(inboundDetails.get(inboundDetails.indexOf(e)).getClientCode());
                inbound.setStatus(InboundStatusEnum.closed.getCode());
                System.out.println("正在修改入库单 = " + inbound);
                //inboundDao.update(inbound);
            }
        }
*/

    }

    @Override
    public void putAway(String referenceNo) {

        Principal principal = SessionLocal.getPrincipal();
        String clientCode = principal.getClientCode();

        //查询入库单
        Inbound inboundDO = inboundDao.queryByReferenceNo(clientCode, referenceNo);
        if (inboundDO == null) {
            throw new WMSException(BizErrorCode.CLIENT_ORDER_SN_NOT_EXIST);
        }
        List<InboundDetail> itemList = inboundDetailsDao.queryByReferenceNo(clientCode, referenceNo);

        Inbound update = new Inbound();
        update.setReferenceNo(referenceNo);
        update.setClientCode(clientCode);
        update.setStatus(InboundStatusEnum.closed.getCode());
        inboundDao.update(update);

        Map<String, Object> map = new HashMap<>();
        map.put("status", 99);
        map.put("client_ordersn", referenceNo);
        map.put("order_type", inboundDO.getAsnType());
        map.put("order_items_list", itemList);
        String data = JSON.toJSONString(map);
        systemService.notifyDataBus(data, clientCode, "wms_inbound_notify");

    }

    @Override
    public PageResult<Inbound> queryBy(InboundParam param) {

        Long count = inboundDao.queryCountBy(param);
        PageResult<Inbound> pageResult = new PageResult<>();
        if (count == 0) {
            return pageResult;
        }
        List<Inbound> list = inboundDao.queryBy(param);
        pageResult.setData(list);
        return pageResult;

    }

    @Override
    public List<ReportInboundDetail> findInboundDetail(InboundDetailParam param, Page page) {
        if(param.getFromDate()==null || param.getToDate()==null){
            throw new WMSException(BizErrorCode.TIME_PARAM_NOT_EXIST);
        }
        page.setCount(wmsInboundDetailDao.queryInboundDetailCount(param));
        return wmsInboundDetailDao.queryInboundDetail(param, page);
    }

}
