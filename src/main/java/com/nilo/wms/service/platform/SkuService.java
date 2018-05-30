package com.nilo.wms.service.platform;



import com.nilo.wms.dto.platform.Sku;
import com.nilo.wms.dto.platform.parameter.SkuParam;

import java.util.List;

public interface SkuService {

    void add(Sku sku);

    void update(Sku sku);

    void delete(String sku);

    List<Sku> queryBy(SkuParam param);


}
