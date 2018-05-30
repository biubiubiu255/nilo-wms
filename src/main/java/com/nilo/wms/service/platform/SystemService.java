package com.nilo.wms.service.platform;

/**
 * 系统服务
 * Created by Administrator on 2017/7/6.
 */
public interface SystemService {

    /**
     * 刷新 wms 客户配置
     */
    void loadingAndRefreshClientConfig();

    /**
     * 刷新 wms 客户配置
     */
    void loadingAndRefreshInterfaceConfig();

    /**
     * 刷新 wms 费用配置
     */
    void loadingAndRefreshWMSFeeConfig();

    /**
     * 刷新 角色权限配置
     */
    void loadingAndRefreshRole();


    void notifyDataBus(String data,String clientCode,String method);
}
