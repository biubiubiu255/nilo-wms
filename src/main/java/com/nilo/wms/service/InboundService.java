package com.nilo.wms.service;

import com.nilo.wms.dto.common.PageResult;
import com.nilo.wms.dto.flux.FluxInbound;
import com.nilo.wms.dto.inbound.InboundHeader;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.dto.platform.parameter.InboundParam;
import com.nilo.wms.dto.platform.system.Permission;

import java.util.List;

/**
 * Created by admin on 2018/3/19.
 */
public interface InboundService {

    void createInBound(InboundHeader inbound);

    void cancelInBound(String referenceNo);

    void confirmASN(List<InboundHeader> list);

    FluxInbound queryFlux(String referenceNo);

    void inboundScan();

    void putAway(String referenceNo);

    PageResult<Inbound> queryBy(InboundParam param);
}
