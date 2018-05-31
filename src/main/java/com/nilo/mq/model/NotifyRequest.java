package com.nilo.mq.model;

import java.util.Map;

/**
 * Created by admin on 2017/11/6.
 */
public class NotifyRequest {

    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String url;

    private Map<String,String> param;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "NotifyRequest{" +
                "url='" + url + '\'' +
                ", param=" + param +
                '}';
    }
}
