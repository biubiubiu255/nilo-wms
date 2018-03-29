package com.nilo.wms.dao.platform;

import com.nilo.wms.common.BaseDao;
import com.nilo.wms.dto.NotifyDO;
import com.nilo.wms.dto.OutboundDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboundDao extends BaseDao<Long, OutboundDO> {

    OutboundDO queryByReferenceNo(String referenceNo);
}
