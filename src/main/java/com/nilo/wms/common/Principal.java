
package com.nilo.wms.common;

import java.io.Serializable;

public class Principal implements Serializable {

    private static final long serialVersionUID = 5798882004228239559L;

    private String clientCode;

    private String method;

    private String customerId;

    private String warehouseId;

    private String userId;

    private String userName;

    private String warehouseCode;

    private String roleId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    @Override
    public String toString() {
        return "Principal{" +
                "clientCode='" + clientCode + '\'' +
                ", method='" + method + '\'' +
                ", customerId='" + customerId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", roleId='" + roleId + '\'' +
                '}';
    }
}