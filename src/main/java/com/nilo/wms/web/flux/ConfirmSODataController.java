package com.nilo.wms.web.flux;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.common.util.XmlUtil;
import com.nilo.wms.dao.platform.ApiLogDao;
import com.nilo.wms.dto.platform.ApiLog;
import com.nilo.wms.service.OutboundService;
import com.nilo.wms.service.platform.RedisUtil;
import com.nilo.wms.web.BaseController;
import com.nilo.wms.web.model.NotifyOrder;
import com.nilo.wms.web.model.SubDelivery;
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

        data = removeXmlDataElement(data, "xmldata");
        //根据wms推送的订单状态进去区分，Udf07（90）：取消； Udf07（99）已完成
        WMSOrderNotify notify = XmlUtil.XMLToBean(data, WMSOrderNotify.class);

        List<String> successList = new ArrayList<>();
        List<String> cancelList = new ArrayList<>();

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
        principal.setWarehouseId("KE01");
        principal.setCustomerId("KILIMALL");
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
            return xmlFailedReturn(e.getMessage());
        }
        addApiLog(data, "confirmSOData", "SUCCESS", true);

        return xmlSuccessReturn();
    }

    @RequestMapping(value = "/confirmCGSOData.html", method = {RequestMethod.POST}, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String confirmCGSOData(String data) {

        data = URLDecoder.decode(data);

        data = removeXmlDataElement(data, "xmldata");
        //根据wms推送的订单状态进去区分，Udf07（90）：取消； Udf07（99）已完成
        WMSOrderNotify notify = XmlUtil.XMLToBean(data, WMSOrderNotify.class);
        Principal principal = new Principal();
        principal.setClientCode("kilimall");
        principal.setWarehouseId("KE01");
        principal.setCustomerId("KILIMALL");
        SessionLocal.setPrincipal(principal);
        List<String> list = new ArrayList<String>();

        for (NotifyOrder order : notify.getList()) {
            list.add(order.getOrderNo());
        }
        try {
            outboundService.confirmSO(list, true);
        } catch (Exception e) {
            addApiLog(data, "confirmCGSOData", e.getMessage(), false);
            return xmlFailedReturn(e.getMessage());
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

    @RequestMapping(value = "/getDeliveryNo.html", method = {RequestMethod.POST})
    @ResponseBody
    public String getDeliveryNo() {
        Long serialNum = RedisUtil.increment("old_waybill_num");
        String waybillNum = "KE2" + String.format("%0" + 7 + "d", serialNum);
        //获取运单号返回给OMS
        return xmlSuccReturnDelivery(waybillNum);
    }

    @RequestMapping(value = "/getSubDelivery.html", method = {RequestMethod.POST})
    @ResponseBody
    public String getSubDelivery(String sign, String data) {
        data = URLDecoder.decode(data);
        //请求参数信息日志输出
        logger.info("getSubDelivery.html sign:{},data{}", sign, data);
        String deliveryNo = "";
        try {
            Principal principal = new Principal();
            principal.setClientCode("kilimall");
            SessionLocal.setPrincipal(principal);
            data = data.substring(data.indexOf("<header>"), data.indexOf("</xmldata>"));
            SubDelivery subDelivery = XmlUtil.XMLToBean(data, SubDelivery.class);
            deliveryNo = outboundService.getSubWaybill(subDelivery.getRelationorderNo());
        } catch (Exception e) {
            logger.error("getSubDelivery failed. data:{}", data, e);
            return xmlFailedReturn(e.getMessage());
        }
        return xmlSuccReturnDelivery(deliveryNo);
    }
}
