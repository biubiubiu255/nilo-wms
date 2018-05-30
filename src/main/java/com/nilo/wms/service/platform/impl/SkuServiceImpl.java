package com.nilo.wms.service.platform.impl;

import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.dao.platform.SkuDao;
import com.nilo.wms.dto.platform.Sku;
import com.nilo.wms.dto.platform.parameter.SkuParam;
import com.nilo.wms.service.platform.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuDao skuDao;

    @Override
    public void add(Sku sku) {

        AssertUtil.isNotNull(sku, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotBlank(sku.getSku(), CheckErrorCode.SKU_EMPTY);
        AssertUtil.isNotNull(sku.getCustomerId(), CheckErrorCode.CUSTOMER_EMPTY);
        AssertUtil.isNotNull(sku.getDesc_c(), CheckErrorCode.DESC_C_EMPTY);
        AssertUtil.isNotNull(sku.getDesc_e(), CheckErrorCode.DESC_E_EMPTY);
        AssertUtil.isNotNull(sku.getPrice(), CheckErrorCode.PRICE_EMPTY);

        Sku query = skuDao.queryBySku(sku.getSku());
        if (query != null) {
            throw new WMSException(BizErrorCode.SKU_EXIST);
        }

        sku.setStatus(1);
        skuDao.insert(sku);
    }

    @Override
    public void update(Sku sku) {

        AssertUtil.isNotNull(sku, SysErrorCode.REQUEST_IS_NULL);
        AssertUtil.isNotBlank(sku.getSku(), CheckErrorCode.SKU_EMPTY);

        Sku query = skuDao.queryBySku(sku.getSku());
        if (query == null) {
            throw new WMSException(BizErrorCode.SKU_NOT_EXIST);
        }
        skuDao.update(sku);

    }

    @Override
    public void delete(String sku) {

        Sku query = skuDao.queryBySku(sku);
        if (query == null) {
            throw new WMSException(BizErrorCode.SKU_NOT_EXIST);
        }
        skuDao.deleteBySku(sku);

    }

    @Override
    public List<Sku> queryBy(SkuParam param) {

        List<Sku> list = new ArrayList<>();

        int count = skuDao.queryByCount(param);
        if (count == 0) return list;

        param.setCount(count);
        list = skuDao.queryBy(param);
        return list;
    }
}
