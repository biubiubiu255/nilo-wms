package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.fee.FeeConfig;
import com.nilo.wms.dto.platform.parameter.FeeConfigParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeConfigDao extends BaseDao<Long, FeeConfig> {

    List<FeeConfig> queryBy(FeeConfigParam param);

    Integer queryCountBy(FeeConfigParam param);

    FeeConfig getBy(@Param("clientCode") String clientCode, @Param("feeType") String feeType, @Param("classType") String classType);

    Integer delete(@Param("clientCode") String clientCode, @Param("feeType") String feeType, @Param("classType") String classType);

}
