package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.platform.Sku;
import com.nilo.wms.dto.platform.parameter.SkuParam;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuDao extends BaseDao<Integer, Sku> {

    int deleteBySku(String sku);

    Long queryByCount(SkuParam param);

    List<Sku> queryBy(SkuParam param);

    Sku queryBySku(String sku);

}