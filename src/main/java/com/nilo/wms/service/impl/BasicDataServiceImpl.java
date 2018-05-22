package com.nilo.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.nilo.mq.model.NotifyRequest;
import com.nilo.mq.producer.AbstractMQProducer;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.DateUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.common.util.XmlUtil;
import com.nilo.wms.dao.flux.SkuDao;
import com.nilo.wms.dto.SkuInfo;
import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.StorageParam;
import com.nilo.wms.dto.SupplierInfo;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.dto.common.InterfaceConfig;
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.flux.FLuxRequest;
import com.nilo.wms.dto.flux.FluxResponse;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.outbound.OutboundItem;
import com.nilo.wms.service.BasicDataService;
import com.nilo.wms.service.HttpRequest;
import com.nilo.wms.service.config.SystemConfig;
import com.nilo.wms.service.system.RedisUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by admin on 2018/3/20.
 */
@Service
public class BasicDataServiceImpl implements BasicDataService {
    private static final Logger logger = LoggerFactory.getLogger(BasicDataServiceImpl.class);

    @Resource(name = "fluxHttpRequest")
    private HttpRequest<FLuxRequest, FluxResponse> fluxHttpRequest;
    @Autowired
    private SkuDao skuDao;
    @Autowired
    @Qualifier("notifyDataBusProducer")
    private AbstractMQProducer notifyDataBusProducer;

    @Override
    public void updateSku(List<SkuInfo> list) {

        AssertUtil.isNotNull(list, SysErrorCode.REQUEST_IS_NULL);
        String customerId = SessionLocal.getPrincipal().getCustomerId();

        for (SkuInfo s : list) {
            AssertUtil.isNotBlank(s.getSku(), CheckErrorCode.SKU_EMPTY);
            AssertUtil.isNotBlank(s.getDescE(), CheckErrorCode.SKU_DESC_EMPTY);
            AssertUtil.isNotBlank(s.getStoreId(), CheckErrorCode.STORE_EMPTY);
            AssertUtil.isNotBlank(s.getFreightClass(), CheckErrorCode.CLASS_EMPTY);

            if (StringUtil.isNotBlank(s.getLogisticsType())) {
                if (!(StringUtil.equals("1", s.getLogisticsType()) || StringUtil.equals("3", s.getLogisticsType()) || StringUtil.equals("2", s.getLogisticsType()))) {
                    throw new WMSException(CheckErrorCode.LOGISTICS_TYPE_ERROR);
                }
            }
            s.setCustomerId(customerId);
        }

        FLuxRequest request = new FLuxRequest();
        String xmlData = XmlUtil.ListToXML(list);
        //添加xml数据前缀
        xmlData = "<xmldata>" + xmlData + "</xmldata>";
        request.setData(xmlData);
        request.setMessageid("SKU");
        request.setMethod("putSKUData");
        //调用flux 请求
        FluxResponse response = fluxHttpRequest.doRequest(request);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getReturnDesc());
        }
    }

    @Override
    public void updateSupplier(List<SupplierInfo> list) {

        AssertUtil.isNotNull(list, SysErrorCode.REQUEST_IS_NULL);
        for (SupplierInfo supplierInfo : list) {
            AssertUtil.isNotBlank(supplierInfo.getCustomerId(), CheckErrorCode.STORE_EMPTY);
            AssertUtil.isNotBlank(supplierInfo.getDescE(), CheckErrorCode.STORE_DESC_EMPTY);
            AssertUtil.isNotBlank(supplierInfo.getType(), CheckErrorCode.STORE_TYPE_EMPTY);
            AssertUtil.isNotBlank(supplierInfo.getAddress(), CheckErrorCode.STORE_ADDRESS_EMPTY);
            AssertUtil.isTrue(StringUtil.equals(supplierInfo.getType(), "1") || StringUtil.equals(supplierInfo.getType(), "2"), CheckErrorCode.STORE_TYPE_NOT_EXIST);

        }

        for (SupplierInfo supplierInfo : list) {
            FLuxRequest request = new FLuxRequest();
            String xmlData = XmlUtil.BeanToXML(supplierInfo);
            //添加xml数据前缀
            xmlData = "<xmldata>" + xmlData + "</xmldata>";
            request.setData(xmlData);
            request.setMessageid("CUSTOMER");
            request.setMethod("putCustData");
            //调用flux 请求
            FluxResponse response = fluxHttpRequest.doRequest(request);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getReturnDesc());
            }
        }
    }

    @Override
    public PageResult<StorageInfo> queryStorageDetail(StorageParam param) {

        AssertUtil.isNotNull(param, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotNull(param.getLimit(), CheckErrorCode.LIMIT_EMPTY);
        AssertUtil.isNotNull(param.getPage(), CheckErrorCode.PAGE_EMPTY);

        Principal principal = SessionLocal.getPrincipal();
        param.setCustomerId(principal.getCustomerId());
        param.setWarehouseId(principal.getWarehouseId());

        PageResult<StorageInfo> pageResult = new PageResult<>();

        Integer count = skuDao.queryCountBy(param);
        pageResult.setCount(count);
        if (count > 0) {
            List<StorageInfo> list = skuDao.queryBy(param);
            for (StorageInfo s : list) {
                String key = RedisUtil.getSkuKey(principal.getClientCode(), s.getSku());
                String lockSto = RedisUtil.hget(key, RedisUtil.LOCK_STORAGE);
                int lockStoInt = ((lockSto == null ? 0 : Integer.parseInt(lockSto)));
                s.setLockStorage(lockStoInt);
            }
            pageResult.setData(list);
        }
        return pageResult;
    }

    @Override
    public List<Map<String, String>> lockStorage(OutboundHeader header) {

        //check
        AssertUtil.isNotNull(header, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotBlank(header.getOrderNo(), CheckErrorCode.CLIENT_ORDER_EMPTY);
        AssertUtil.isNotNull(header.getItemList(), CheckErrorCode.ITEM_EMPTY);
        for (OutboundItem item : header.getItemList()) {
            AssertUtil.isNotBlank(item.getSku(), CheckErrorCode.SKU_EMPTY);
            AssertUtil.isNotNull(item.getQty(), CheckErrorCode.QTY_EMPTY);
        }
        String clientCode = SessionLocal.getPrincipal().getClientCode();

        // 判断订单号是否已锁定
        String orderNoKey = RedisUtil.getLockOrderKey(clientCode, header.getOrderNo());
        boolean keyExist = RedisUtil.hasKey(orderNoKey);
        if (keyExist) {
            return null;
        }

        //获取redis锁
        Jedis jedis = RedisUtil.getResource();
        String requestId = UUID.randomUUID().toString();
        RedisUtil.tryGetDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        List<Map<String, String>> result = new ArrayList<>();

        Map<String, Integer> lockRecord = new HashMap<>();
        boolean lockSuccess = true;
        for (OutboundItem item : header.getItemList()) {
            String sku = item.getSku();
            int qty = item.getQty();
            String key = RedisUtil.getSkuKey(clientCode, sku);
            String lockSto = jedis.hget(key, RedisUtil.LOCK_STORAGE);
            int lockStoInt = lockSto == null ? 0 : Integer.parseInt(lockSto);
            String sto = jedis.hget(key, RedisUtil.STORAGE);
            int stoInt = sto == null ? 0 : Integer.parseInt(sto);
            //校验库存是否足够
            if (sto == null || lockStoInt + qty > stoInt) {
                Map<String, String> notEnoughMap = new HashMap<>();
                notEnoughMap.put("sku", sku);
                notEnoughMap.put("storage", "" + stoInt);
                notEnoughMap.put("lock_storage", "" + lockStoInt);
                result.add(notEnoughMap);
                lockSuccess = false;
                continue;
            }
            //记录锁定
            lockRecord.put(key, lockStoInt + qty);
        }


        if (!lockSuccess) {
            RedisUtil.releaseDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);
            return result;
        }

        for (Map.Entry<String, Integer> entry : lockRecord.entrySet()) {
            jedis.hset(entry.getKey(), RedisUtil.LOCK_STORAGE, "" + entry.getValue());
        }

        RedisUtil.releaseDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        //锁定成功，添加订单到锁定列表
        for (OutboundItem item : header.getItemList()) {
            RedisUtil.hset(orderNoKey, item.getSku(), "" + item.getQty());
        }
        RedisUtil.hset(orderNoKey, RedisUtil.LOCK_TIME, DateUtil.formatCurrent(DateUtil.LONG_WEB_FORMAT));

        return null;
    }


    @Override
    public void unLockStorage(String orderNo) {

        //check
        AssertUtil.isNotBlank(orderNo, CheckErrorCode.CLIENT_ORDER_EMPTY);

        String clientCode = SessionLocal.getPrincipal().getClientCode();

        // 判断订单号是否已锁定
        String orderNoKey = RedisUtil.getLockOrderKey(clientCode, orderNo);
        boolean keyExist = RedisUtil.hasKey(orderNoKey);
        if (!keyExist) {
            return;
        }

        //获取redis锁
        Jedis jedis = RedisUtil.getResource();
        String requestId = UUID.randomUUID().toString();
        RedisUtil.tryGetDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);
        // 查询锁定列表
        Set<String> skuList = jedis.hkeys(orderNoKey);
        skuList.remove(RedisUtil.LOCK_TIME);
        for (String sku : skuList) {
            int qty = Integer.parseInt(jedis.hget(orderNoKey, sku));
            String key = RedisUtil.getSkuKey(clientCode, sku);
            String lockSto = jedis.hget(key, RedisUtil.LOCK_STORAGE);
            int afterLockStorage = Integer.parseInt(lockSto) - qty;
            jedis.hset(key, RedisUtil.LOCK_STORAGE, "" + afterLockStorage);
        }

        RedisUtil.releaseDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        RedisUtil.del(orderNoKey);

    }

    @Override
    public void successStorage(OutboundHeader header) {

        String clientCode = SessionLocal.getPrincipal().getClientCode();

        //低于安全库存
        List<StorageInfo> lessThanSafe = new ArrayList<>();

        //获取redis锁
        Jedis jedis = RedisUtil.getResource();
        String requestId = UUID.randomUUID().toString();
        RedisUtil.tryGetDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        // 判断订单号是否锁定库存过
        String orderNoKey = RedisUtil.getLockOrderKey(clientCode, header.getOrderNo());
        boolean keyExist = RedisUtil.hasKey(orderNoKey);
        //锁定库存记录存在，则扣减锁定库存及库存
        if (keyExist) {
            // 查询锁定列表
            Set<String> skuList = jedis.hkeys(orderNoKey);
            skuList.remove(RedisUtil.LOCK_TIME);
            for (String sku : skuList) {
                int qty = Integer.parseInt(jedis.hget(orderNoKey, sku));
                //扣减锁定库存
                String key = RedisUtil.getSkuKey(clientCode, sku);
                String lockSto = jedis.hget(key, RedisUtil.LOCK_STORAGE);
                int afterLockStorage = Integer.parseInt(lockSto) - qty;
                jedis.hset(key, RedisUtil.LOCK_STORAGE, "" + afterLockStorage);

                //扣减库存
                String sto = jedis.hget(key, RedisUtil.STORAGE);
                int stoInt = Integer.parseInt(sto) - qty;
                jedis.hset(key, RedisUtil.STORAGE, "" + stoInt);

                //库存少于安全库存 发送通知
                String safeSto = jedis.hget(key, RedisUtil.SAFE_STORAGE);
                int safeInt = Integer.parseInt(safeSto == null ? "0" : safeSto);
                if (stoInt < safeInt) {
                    String storeId = jedis.hget(key, RedisUtil.STORE);
                    StorageInfo info = new StorageInfo();
                    info.setSku(sku);
                    info.setStoreId(storeId);
                    info.setSafeStorage(safeInt);
                    info.setStorage(stoInt);
                    lessThanSafe.add(info);
                }
            }
            RedisUtil.del(orderNoKey);
        }
        RedisUtil.releaseDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        if (lessThanSafe.size() == 0) {
            return;
        }
        ClientConfig clientConfig = SystemConfig.getClientConfig().get(clientCode);
        InterfaceConfig interfaceConfig = SystemConfig.getInterfaceConfig().get(clientCode).get("storage_not_enough");

        Map<String, Object> map = new HashMap<>();
        map.put("list", lessThanSafe);
        String data = JSON.toJSONString(map);
        Map<String, String> params = new HashMap<>();
        params.put("method", interfaceConfig.getMethod());
        params.put("sign", createNOSSign(data, clientConfig.getClientKey()));
        params.put("data", data);
        params.put("app_key", "wms");
        params.put("request_id", UUID.randomUUID().toString());
        params.put("timestamp", "" + DateUtil.getSysTimeStamp());
        NotifyRequest notify = new NotifyRequest();
        notify.setParam(params);
        notify.setUrl(interfaceConfig.getUrl());
        try {
            notifyDataBusProducer.sendMessage(notify);
        } catch (Exception e) {
            logger.error("confirmSO send message failed.", e);
        }
    }

    @Override
    public void syncStock(String clientCode) {

        AssertUtil.isNotBlank(clientCode, CheckErrorCode.APP_KEY_EMPTY);
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

        StorageParam param = new StorageParam();
        param.setWarehouseId(config.getWarehouseCode());
        param.setCustomerId(config.getCustomerCode());
        param.setPage(1);
        param.setLimit(10000);
        List<StorageInfo> list = skuDao.queryBy(param);
        if (list == null || list.size() == 0) return;
        Set<String> cacheSkuList = RedisUtil.keys(RedisUtil.getSkuKey(clientCode, "*"));

        //获取redis锁
        Jedis jedis = RedisUtil.getResource();
        String requestId = UUID.randomUUID().toString();
        RedisUtil.tryGetDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);

        //构建差异数据
        List<StorageInfo> diffList = new ArrayList<>();
        for (StorageInfo s : list) {
            String key = RedisUtil.getSkuKey(clientCode, s.getSku());
            String storage = RedisUtil.hget(key, RedisUtil.STORAGE);
            if (!StringUtil.equals(storage, "" + s.getStorage())) {
                String lockStorage = RedisUtil.hget(key, RedisUtil.LOCK_STORAGE);
                s.setLockStorage(lockStorage == null ? 0 : Integer.parseInt(lockStorage));
                diffList.add(s);
                continue;
            }
        }

        //删掉 cache中多余的sku
        Map<String, StorageInfo> wmsSkuList = new HashMap<>();
        for (StorageInfo s : list) {
            wmsSkuList.put(s.getSku(), s);
        }
        for (String s : cacheSkuList) {
            String[] temp = s.split("_sku_");
            if (!wmsSkuList.containsKey(temp[1])) {
                String key = RedisUtil.getSkuKey(clientCode, temp[1]);
                jedis.del(key);
            }
        }

        for (StorageInfo s : diffList) {
            String key = RedisUtil.getSkuKey(clientCode, s.getSku());
            RedisUtil.hset(key, RedisUtil.STORAGE, "" + s.getStorage());
            RedisUtil.hset(key, RedisUtil.LOCK_STORAGE, "" + s.getLockStorage());
            RedisUtil.hset(key, RedisUtil.STORE, s.getStoreId());
            RedisUtil.hset(key, RedisUtil.SAFE_STORAGE, "" + s.getSafeStorage());
        }

        RedisUtil.releaseDistributedLock(jedis, RedisUtil.LOCK_KEY, requestId);
        //通知上游系统 库存变更
        storageChangeNotify(diffList);
    }

    @Override
    public void storageChangeNotify(List<StorageInfo> list) {

        if (list == null || list.size() == 0) return;

        String clientCode = SessionLocal.getPrincipal().getClientCode();
        ClientConfig clientConfig = SystemConfig.getClientConfig().get(clientCode);
        InterfaceConfig interfaceConfig = SystemConfig.getInterfaceConfig().get(clientCode).get("storage_change_notify");

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        String data = JSON.toJSONString(map);
        Map<String, String> params = new HashMap<>();
        params.put("method", interfaceConfig.getMethod());
        params.put("sign", createNOSSign(data, clientConfig.getClientKey()));
        params.put("data", data);
        params.put("app_key", "wms");
        params.put("request_id", UUID.randomUUID().toString());
        params.put("timestamp", "" + DateUtil.getSysTimeStamp());

        NotifyRequest notify = new NotifyRequest();
        notify.setParam(params);
        notify.setUrl(interfaceConfig.getUrl());
        try {
            notifyDataBusProducer.sendMessage(notify);
        } catch (Exception e) {
            logger.error("confirmSO send message failed.", e);
        }
    }

    private String createNOSSign(String data, String key) {
        String str = key + data + key;
        return DigestUtils.md5Hex(str).toUpperCase();
    }

}
