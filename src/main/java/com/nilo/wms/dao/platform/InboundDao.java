package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.dto.platform.inbound.InboundDetail;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundDao extends BaseDao<Long, Inbound> {

    Inbound queryByReferenceNo(@Param(value="clientCode")String clientCode, @Param(value="referenceNo")String referenceNo);

    List<Inbound> queryBy(InboundParam param);

    Long queryCountBy(InboundParam param);


}
