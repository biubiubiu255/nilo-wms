package com.nilo.wms.dao.flux;

import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.platform.parameter.StorageParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/4/24.
 */
@Repository
public interface FluxInventoryDao {

    List<StorageInfo> queryBy(@Param("param") StorageParam param);

    Integer queryCountBy(@Param("param") StorageParam param);

    void updateSafeQty(@Param("customerId") String customerId,@Param("sku") String sku,@Param("safeQty") String safeQty);
}
