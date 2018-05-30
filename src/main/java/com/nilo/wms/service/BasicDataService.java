package com.nilo.wms.service;

import com.nilo.wms.dto.SkuInfo;
import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.SupplierInfo;
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.platform.parameter.StorageParam;

import java.util.List;

/**
 * Created by admin on 2018/3/19.
 */
public interface BasicDataService {

    void updateSku(List<SkuInfo> list);

    void updateSupplier(List<SupplierInfo> list);

    PageResult<StorageInfo> queryStorageDetail(StorageParam param);

    List<StorageInfo> lockStorage(OutboundHeader header);

    void unLockStorage(String orderNo);

    void successStorage(OutboundHeader header);

    void syncStock(String clientCode);

    void storageChangeNotify(List<StorageInfo> list);

    void updateStorage(String sku, Integer cacheStorage, Integer lockStorage, Integer safeStorage);

    void sync(List<String> sku);
}
