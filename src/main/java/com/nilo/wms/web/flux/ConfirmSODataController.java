package com.nilo.wms.web.flux;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.common.util.XmlUtil;
import com.nilo.wms.dao.platform.ApiLogDao;
import com.nilo.wms.dto.platform.ApiLog;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.web.BaseController;
import com.nilo.wms.web.model.NotifyOrder;
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
public class ConfirmSODataController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ApiLogDao apiLogDao;
    @Autowired
    private OutboundService outboundService;

    @RequestMapping(value = "/confirmSOData.html", method = {RequestMethod.POST}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String confirmSOData(String data) {
        data = URLDecoder.decode(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Request confirmSOData.html -data:{}", data);
        }
        data = removeXmlDataElement(data, "xmldata");
        //根据wms推送的订单状态进去区分，Udf07（90）：取消； Udf07（99）已完成
        WMSOrderNotify notify = XmlUtil.XMLToBean(data, WMSOrderNotify.class);

        List<String> successList = new ArrayList<String>();
        List<String> cancelList = new ArrayList<String>();

        for (NotifyOrder order : notify.getList()) {
            //订单状态
            String orderStatus = order.getOrderStatus().trim();
            if (StringUtil.equals(orderStatus, "90")) {
                cancelList.add(order.getOrderNo());
            }
            if (StringUtil.equals(orderStatus, "99")) {
                successList.add(order.getOrderNo());
            }
        }
        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        SessionLocal.setPrincipal(principal);
        try {
            if (successList.size() > 0) {
                outboundService.confirmSO(successList, true);
            }
            if (cancelList.size() > 0) {
                outboundService.confirmSO(cancelList, false);
            }
        } catch (Exception e) {
            addApiLog(data, "confirmSOData", e.getMessage(), false);
            throw e;
        }
        addApiLog(data, "confirmSOData", "SUCCESS", true);

        return xmlSuccessReturn();
    }

    @RequestMapping(value = "/confirmCGSOData.html", method = {RequestMethod.POST}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String confirmCGSOData(String data) {

        data = URLDecoder.decode(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Request confirmCGSOData.html -data:{}", data);
        }
        data = removeXmlDataElement(data, "xmldata");
        //根据wms推送的订单状态进去区分，Udf07（90）：取消； Udf07（99）已完成
        WMSOrderNotify notify = XmlUtil.XMLToBean(data, WMSOrderNotify.class);
        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        SessionLocal.setPrincipal(principal);
        List<String> list = new ArrayList<String>();

        for (NotifyOrder order : notify.getList()) {
            list.add(order.getOrderNo());
        }
        try {
            outboundService.confirmSO(list, true);
        } catch (Exception e) {
            addApiLog(data, "confirmCGSOData", e.getMessage(), false);
        }
        addApiLog(data, "confirmCGSOData", "SUCCESS", true);
        return xmlSuccessReturn();

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
