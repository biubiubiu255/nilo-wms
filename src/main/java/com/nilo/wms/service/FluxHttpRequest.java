
/**
 * KILIMALL.com Inc.
 * Copyright (c) 2015-2016 All Rights Reserved.
 */
package com.nilo.wms.service;

import com.nilo.wms.common.util.HttpUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.flux.FLuxRequest;
import com.nilo.wms.dto.flux.FluxResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 富勒Http请求网关
 *
 * @author ronny.zeng
 * @version $Id: FluxHttpRequestImpl.java, v 0.1 2016年6月20日 下午4:48:05 ronny.zeng Exp $
 */
@Service("fluxHttpRequest")
public class FluxHttpRequest implements HttpRequest<FLuxRequest, FluxResponse> {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(FluxHttpRequest.class);

    private static final String SUCCESS_FLAG = "1", FAILED_FLAG = "0";

    /**
     * DATAHUB 客户编码
     */
    @Value("#{configProperties['flux_client_customerid']}")
    private String client_customerid;
    /**
     * 固定值“FLUXWMS”
     */
    @Value("#{configProperties['flux_client_db']}")
    private String client_db;
    /**
     * Token 号
     */
    @Value("#{configProperties['flux_app_token']}")
    private String app_token;
    /**
     */
    @Value("#{configProperties['flux_app_key']}")
    private String app_key;

    /**
     * omsNoticUrl
     */
    @Value("#{configProperties['flux_url']}")
    private String flux_url;

    @Value("#{configProperties['flux_key']}")
    private String key;

    @Value("#{configProperties['flux_status']}")
    private String flux_status;

    /**
     * 请求
     *
     * @param fLuxRequest
     * @return
     */
    @Override
    public FluxResponse doRequest(FLuxRequest fLuxRequest) {
        // 返回值
        FluxResponse response = new FluxResponse();
        if (StringUtil.equals(flux_status, "close")) {
            response.setReturnFlag("1");
            return response;
        }

        Map<String, String> param = new HashMap<>();
        //client_customerid
        param.put("client_customerid", client_customerid);
        //client_db
        param.put("client_db", client_db);
        //Token 号
        param.put("apptoken", app_token);
        //验签KEY
        param.put("appkey", app_key);
        // 时间戳
        param.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        // 请求格式
        param.put("format", fLuxRequest.getFormat());
        param.put("method", fLuxRequest.getMethod());
        param.put("messageid", fLuxRequest.getMessageid());
        //业务数据
        try {
            param.put("data", URLEncoder.encode(fLuxRequest.getData(), "utf-8"));
            //加签值
            param.put("sign", createFluxSign(fLuxRequest.getData()));
            // 请求flux
            logger.info("Flux Request url:{} params:{}", flux_url, param);
            String responseStr = HttpUtil.post(flux_url, param);
            responseStr = URLDecoder.decode(responseStr, "utf-8");
            logger.info("reponse:{}", responseStr);
            //校验结果
            if (StringUtil.isEmpty(responseStr) || !isXML(responseStr)) {
                response.setReturnCode("99999");
                response.setReturnDesc("Error");
                response.setReturnFlag(FAILED_FLAG);
                return response;
            }
            responseStr = responseStr.replace("<ordernos><orderinfo>", "").replace("</orderinfo></ordernos>", "");
            Document document = DocumentHelper.parseText(responseStr);
            //获取根节点元素对象
            Element root = document.getRootElement();
            Element returnElement = root.element("return");
            //获取属性值
            response.setReturnFlag(returnElement.element("returnFlag").getTextTrim());
            response.setReturnCode(returnElement.element("returnCode").getTextTrim());
            response.setReturnDesc(returnElement.element("returnDesc").getTextTrim());

            Element ordersE = root.element("Status");
            if (ordersE != null) {
                response.setResultInfo(ordersE.getTextTrim());
            }
            /*Element resultInfo = (Element) returnElement.selectSingleNode("//resultInfo");
            if(resultInfo != null){
                response.setResultInfo(resultInfo.getTextTrim());
            }*/
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    private String createFluxSign(String data) {
        //appSecret + data +appSecret 进行MD5加密
        String md5Str = "";
        try {
            md5Str = DigestUtils.md5Hex((key + data + key).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
        }
        //md5Str加密串进行base64加密
        String base64Str;
        String sign = "";
        try {
            base64Str = new BASE64Encoder().encode(md5Str.getBytes("utf-8"));
            //转换成大写，utf-8 urlEncoding
            sign = URLEncoder.encode(base64Str.toUpperCase(), "utf-8");
        } catch (UnsupportedEncodingException e) {
        }

        return sign;
    }

    /**
     * 判断是否是xml结构
     */
    public static boolean isXML(String value) {
        try {
            DocumentHelper.parseText(value);
        } catch (DocumentException e) {
            return false;
        }
        return true;
    }

}
