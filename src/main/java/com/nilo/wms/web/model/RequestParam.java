package com.nilo.wms.web.model;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.common.enums.MethodEnum;
import com.nilo.wms.common.exception.BizErrorCode;
import com.nilo.wms.common.exception.CheckErrorCode;
import com.nilo.wms.common.exception.WMSException;
import com.nilo.wms.common.util.AssertUtil;
import com.nilo.wms.common.util.StringUtil;
import com.nilo.wms.dto.common.ClientConfig;
import com.nilo.wms.service.config.SystemConfig;
import com.nilo.wms.service.platform.RedisUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by admin on 2017/9/28.
 */
public class RequestParam {

    private String data;

    private String sign;

    private String app_key;

    private String request_id;

    private Long timestamp;

    private String method;

    private String country_code;

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public MethodEnum getMethod() {
        return MethodEnum.getEnum(this.method);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void checkParam() {
        AssertUtil.isNotBlank(request_id, CheckErrorCode.REQUEST_ID_EMPTY);
        AssertUtil.isNotBlank(app_key, CheckErrorCode.APP_KEY_EMPTY);
        AssertUtil.isNotBlank(data, CheckErrorCode.DATA_EMPTY);
        AssertUtil.isNotBlank(sign, CheckErrorCode.SIGN_EMPTY);
        AssertUtil.isNotBlank(method, CheckErrorCode.Method_EMPTY);
        AssertUtil.isNotNull(timestamp, CheckErrorCode.TIMESTAMP_EMPTY);
        AssertUtil.isNotNull(getMethod(), CheckErrorCode.METHOD_NOT_EXIST);

        //时间戳 10分钟内
        /*Long currentTime = DateUtil.getSysTimeStamp();
        Long diff = Math.abs(currentTime - this.timestamp);
        if (diff > 10 * 60) {
            throw new WMSException(CheckErrorCode.TIMESTAMP_ERROR);
        }*/
        try {
            data = URLDecoder.decode(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new WMSException(CheckErrorCode.DECODE_ERROR);
        }

        ClientConfig config = SystemConfig.getClientConfig().get(app_key);
        //校验sign
        if (config == null) throw new WMSException(BizErrorCode.APP_KEY_NOT_EXIST);
        boolean check = checkSign(config.getWmsKey(), data, sign);
        AssertUtil.isTrue(check, BizErrorCode.SIGN_ERROR);

        //校验request_id是否已经存在
        String value = RedisUtil.get(request_id);
        if(StringUtil.isNotEmpty(value)){
            throw new IllegalStateException("request already exist");
        }

        RedisUtil.set(request_id,"1",5);

        //设置调用api主体信息
        Principal principal = new Principal();
        principal.setClientCode(app_key);
        principal.setMethod(method);
        principal.setCustomerId(config.getCustomerCode());
        principal.setWarehouseId(config.getWarehouseCode());
        SessionLocal.setPrincipal(principal);


    }

    private boolean checkSign(String key, String data, String sign) {
        String checkSign = DigestUtils.md5Hex(key + data + key);
        return StringUtil.equalsIgnoreCase(checkSign, sign);
    }

    @Override
    public String toString() {
        return "RequestParam{" +
                "data='" + data + '\'' +
                ", sign='" + sign + '\'' +
                ", app_key='" + app_key + '\'' +
                ", request_id='" + request_id + '\'' +
                ", timestamp=" + timestamp +
                ", method='" + method + '\'' +
                '}';
    }
}
