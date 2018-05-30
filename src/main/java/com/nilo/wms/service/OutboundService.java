package com.nilo.wms.service;

import com.nilo.wms.dto.flux.FluxOutbound;
import com.nilo.wms.dto.outbound.OutboundHeader;

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
}
