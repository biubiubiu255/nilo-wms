package com.nilo.wms.dao.flux;

import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.outbound.OutboundDetailParam;
import com.nilo.wms.dto.outbound.ReportOutboundDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/4/24.
 */
@Repository
public interface WMSOutboundDetailDao {

    List<ReportOutboundDetail> queryOutboundDetail(@Param("param") OutboundDetailParam param, @Param("page") Page page);

    Integer queryOutboundDetailCount(@Param("param") OutboundDetailParam param);
}
