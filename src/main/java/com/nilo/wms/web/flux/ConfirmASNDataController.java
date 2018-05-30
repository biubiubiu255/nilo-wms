/**
 * KILIMALL.com Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.web.flux;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.util.XmlUtil;
import com.nilo.wms.dao.platform.ApiLogDao;
import com.nilo.wms.dto.inbound.InboundHeader;
import com.nilo.wms.dto.inbound.InboundItem;
import com.nilo.wms.dto.platform.ApiLog;
import com.nilo.wms.service.InboundService;
import com.nilo.wms.web.BaseController;
import com.nilo.wms.web.model.NotifyOrder;
import com.nilo.wms.web.model.NotifyOrderItem;
import com.nilo.wms.web.model.WMSOrderNotify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ConfirmASNDataController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApiLogDao apiLogDao;
    @Autowired
    private InboundService inboundService;

    @RequestMapping(value = "/confirmASNData.html", method = {RequestMethod.POST}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String confirmASNData(String data) {
        data = URLDecoder.decode(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Request confirmASNData.html -data:{}", data);
        }
        data = removeXmlDataElement(data, "xmldata");

        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        SessionLocal.setPrincipal(principal);
        try {
            inboundService.confirmASN(buildASNInfo(data));
        } catch (Exception e) {
            addApiLog(data, "confirmASNData", e.getMessage(), false);
            return xmlFailedReturn(e.getMessage());
        }
        addApiLog(data, "confirmASNData", "SUCCESS", true);
        return xmlSuccessReturn();

    }

    @RequestMapping(value = "/confirmTRASNData.html", method = {RequestMethod.POST}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String confirmTRASNData(String data) {

        data = URLDecoder.decode(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Request confirmTRASNData.html -data:{}", data);
        }
        data = removeXmlDataElement(data, "xmldata");

        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        SessionLocal.setPrincipal(principal);

        try {
            inboundService.confirmASN(buildASNInfo(data));
        } catch (Exception e) {
            addApiLog(data, "confirmTRASNData", e.getMessage(), false);
            return xmlFailedReturn(e.getMessage());
        }
        addApiLog(data, "confirmTRASNData", "SUCCESS", true);
        return xmlSuccessReturn();

    }

    private List<InboundHeader> buildASNInfo(String data) {

        WMSOrderNotify notify = XmlUtil.XMLToBean(data, WMSOrderNotify.class);

        List<InboundHeader> list = new ArrayList<>();
        for (NotifyOrder n : notify.getList()) {
            InboundHeader asn = new InboundHeader();
            asn.setReferenceNo(n.getOrderNo());
            List<InboundItem> itemList = new ArrayList<>();
            for (NotifyOrderItem i : n.getItem()) {
                InboundItem item = new InboundItem();
                item.setSku(i.getSku());
                item.setQty(i.getQty());
                itemList.add(item);
            }
            asn.setItemList(itemList);
            list.add(asn);
        }
        return list;
    }

    private void addApiLog(String data, String method, String response, boolean result) {

        ApiLog log = new ApiLog();
        log.setAppKey("flux");
        log.setData(HtmlUtils.htmlEscape(data));
        log.setMethod(method);
        log.setSign("");
        log.setResponse(response);
        log.setStatus(result ? 1 : 0);
        apiLogDao.insert(log);
    }
}
