package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.platform.outbound.Outbound;
import com.nilo.wms.dto.platform.parameter.OutboundParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundDao extends BaseDao<Long, Outbound> {

    Outbound queryByReferenceNo(@Param(value="clientCode")String clientCode, @Param(value="referenceNo")String referenceNo);

    List<Outbound> queryByList(@Param(value="clientCode")String clientCode, @Param(value="referenceNoList")List<String> referenceNoList);

    List<Outbound> queryBy(OutboundParam param);

    Long queryCountBy(OutboundParam param);
}
