package com.nilo.wms.dao.flux;

import com.nilo.wms.dto.fee.FeeDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/4/24.
 */
@Repository
public interface WMSFeeDao {

    List<FeeDO> queryStorage(@Param(value = "customerId") String customerId, @Param(value = "warehouseId")String warehouseId);

    List<FeeDO> queryInBoundOrderHandler(@Param(value = "customerId") String customerId, @Param(value = "warehouseId")String warehouseId, @Param(value = "fromDate")String fromDate, @Param(value = "toDate")String toDate);

    List<FeeDO> queryOrderHandler(@Param(value = "customerId") String customerId, @Param(value = "warehouseId")String warehouseId, @Param(value = "fromDate")String fromDate, @Param(value = "toDate")String toDate);

    List<FeeDO> queryOrderReturn(@Param(value = "customerId") String customerId, @Param(value = "warehouseId")String warehouseId, @Param(value = "fromDate")String fromDate, @Param(value = "toDate")String toDate);

    List<FeeDO> queryReturnMerchant(@Param(value = "customerId") String customerId, @Param(value = "warehouseId")String warehouseId, @Param(value = "fromDate")String fromDate, @Param(value = "toDate")String toDate);
}
