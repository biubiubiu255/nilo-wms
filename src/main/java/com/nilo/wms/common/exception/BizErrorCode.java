
package com.nilo.wms.common.exception;

/**
 * Created by ronny on 2017/8/23.
 */
public enum BizErrorCode implements ErrorCode {

    APP_KEY_NOT_EXIST("app_key not exist", "200001"),
    SIGN_ERROR("sign error", "200002"),
    NOT_EXIST("{0} not exist.", "200003"),
    OUTBOUND_ALREADY_EXIST("referenceNo {0} already exist.", "200004"),
    INBOUND_ALREADY_EXIST("referenceNo {0} already exist.", "200005"),
    STORAGE_NOT_ENOUGH("inventory not enough.", "200006"),
    OUTBOUND_NOT_EXIST("referenceNo not exist.", "200007"),
    ORDER_TYPE_NOT_EXIST("order_type not exist.", "20008"),
    RANDOM_CODE_ERROR("random code error.", "20009"),
    USERNAME_PASSWORD_ERROR("username or password error.", "200010"),
    USERNAME_EXIST("username is already exist.", "200011"),
    PERMISSION_ID_EXIST("permissionId is already exist.", "200013"),
    NOT_LOCK_STORAGE("need lock inventory first", "200014"),
    CLIENT_ORDER_SN_NOT_EXIST("client_ordersn not exist", "200015"),
    SKU_NOT_EXIST("sku not exist", "200016"),
    SKU_EXIST("sku exist", "200017"),
    ;

    private final String description;
    private final String code;

    private BizErrorCode(String description, String code) {
        this.description = description;
        this.code = code;
    }

    @Override
    public ExceptionType getType() {
        return ExceptionType.BIZ_VERIFY_ERROR;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

}



