package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.inbound.InboundItem;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.dto.platform.inbound.InboundDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundDetailsDao extends BaseDao<Long, InboundDetail> {

    int insertBatch(List<InboundDetail> inboundDetail);

    List<InboundDetail> queryByReferenceNo(@Param(value="clientCode")String clientCode, @Param(value="referenceNo")String referenceNo);


}
