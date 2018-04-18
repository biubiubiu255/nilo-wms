
package com.nilo.wms.common.exception;

/**
 * Created by ronny on 2017/8/23.
 */
public enum BizErrorCode implements ErrorCode {

    APP_KEY_NOT_EXIST("app_key not exist", "200001"),
    SIGN_ERROR("Sign error", "200002"),
    NOT_EXIST("{0} not exist.", "200003"),
    OUTBOUND_ALREADY_EXIST("OutBound {0} already exist.", "200004"),
    INBOUND_ALREADY_EXIST("ASN {0} already exist.", "200005"),
    STORAGE_NOT_ENOUGH("Sku {0} storage not enough.", "200006"),
    OUTBOUND_NOT_EXIST("OutBound not exist.", "200007"),
    ORDER_TYPE_NOT_EXIST("order_type not exist.", "100031"),
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



