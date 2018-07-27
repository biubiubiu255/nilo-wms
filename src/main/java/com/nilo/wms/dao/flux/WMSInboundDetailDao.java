package com.nilo.wms.dao.flux;

import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.inbound.InboundDetailParam;
import com.nilo.wms.dto.inbound.ReportInboundDetail;
import com.nilo.wms.dto.outbound.OutboundDetailParam;
import com.nilo.wms.dto.outbound.ReportOutboundDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/4/24.
 */
@Repository
public interface WMSInboundDetailDao {

    List<ReportInboundDetail> queryInboundDetail(@Param("param") InboundDetailParam param, @Param("page") Page page);

    Integer queryInboundDetailCount(@Param("param") InboundDetailParam param);
}
