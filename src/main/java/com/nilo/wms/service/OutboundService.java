package com.nilo.wms.service;

import com.nilo.wms.dto.common.Page;
import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.flux.FluxOutbound;
import com.nilo.wms.dto.outbound.OutboundDetailParam;
import com.nilo.wms.dto.outbound.ReportOutboundDetail;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.platform.outbound.Outbound;
import com.nilo.wms.dto.platform.parameter.OutboundParam;

import java.util.List;

/**
 * Created by admin on 2018/3/19.
 */
public interface OutboundService {

    void createOutBound(OutboundHeader outBound);

    void cancelOutBound(String orderNo);

    void confirmSO(List<String> list, boolean result);

    String getSubWaybill(String orderNo);

    FluxOutbound queryFlux(String orderNo);

    void ship(String referenceNo);

    void cancel(String referenceNo);

    PageResult<Outbound> queryBy(OutboundParam param);

    List<ReportOutboundDetail> findOutboundDetail(OutboundDetailParam param, Page page);
}
