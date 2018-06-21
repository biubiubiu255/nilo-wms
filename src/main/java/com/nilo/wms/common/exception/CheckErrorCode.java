
package com.nilo.wms.common.exception;

/**
 * Created by ronny on 2017/8/23.
 */
public enum CheckErrorCode implements ErrorCode {
    REQUEST_ID_EMPTY("request_id is empty", "100000"),
    APP_KEY_EMPTY("app_key is empty", "100000"),
    DATA_EMPTY("data is empty.", "100001"),
    SIGN_EMPTY("sign is empty.", "100002"),
    Method_EMPTY("method is empty.", "100003"),
    TIMESTAMP_EMPTY("timestamp is empty.", "100004"),
    METHOD_NOT_EXIST("method is not exist.", "100005"),
    TIMESTAMP_ERROR("Request timeout", "100006"),
    SING_ERROR("Sign is error.", "100007"),
    DECODE_ERROR("UrlDecode error.", "100008"),
    CLIENT_ORDER_EMPTY("client_ordersn empty.", "100009"),
    WAYBILL_EMPTY("waybill_number empty.", "100010"),
    ORDER_AMOUNT_EMPTY("Order Amount Empty.", "100011"),
    ASN_NO_EMPTY("Asn No. Empty.", "100012"),
    ASN_TYPE_EMPTY("Asn Type Empty.", "100013"),
    SUPPLIER_NAME_EMPTY("Supplier Name Empty.", "100014"),
    SUPPLIER_ID_EMPTY("Supplier ID Empty.", "100015"),
    CUSTOMER_EMPTY("Client ID Empty.", "100016"),
    WAREHOUSE_EMPTY("Warehouse ID Empty.", "100017"),
    STORE_EMPTY("store_id empty.", "100018"),
    STORE_DESC_EMPTY("store_name empty.", "100019"),
    SKU_EMPTY(" sku empty.", "100019"),
    SKU_DESC_EMPTY("goods_name empty.", "100020"),
    ADD_TIME_EMPTY("add_time empty.", "100021"),
    ITEM_EMPTY("order_item_list empty.", "100022"),
    QTY_EMPTY("qty empty.", "100023"),
    ORDER_TYPE_EMPTY("order_type empty.", "100024"),
    DELIVERY_TYPE_EMPTY("delivery_type empty.", "100025"),
    IS_POD_EMPTY("is_pod empty.", "100026"),
    RECEIVER_INFO_EMPTY("receiver_info empty.", "100027"),
    RECEIVER_NAME_EMPTY("receiver name empty.", "100028"),
    RECEIVER_ADDRESS_EMPTY("receiver address empty.", "100029"),
    RECEIVER_PHONE_EMPTY("receiver contact_number empty.", "100030"),
    CLIENT_ID_EMPTY("client_id empty.", "100031"),
    STORE_ADDRESS_EMPTY("address empty.", "100032"),
    STORE_TYPE_EMPTY("type empty.", "100033"),
    STORE_TYPE_NOT_EXIST("type not exist.", "100034"),
    USER_NAME_EMPTY("user name empty.", "100035"),
    PASSWORD_EMPTY("password empty.", "100036"),
    RANDOM_CODE_EMPTY("random code empty.", "100037"),
    PAGE_EMPTY("page empty.", "100038"),
    LIMIT_EMPTY("limit empty.", "100039"),
    ROLE_ID_EMPTY("RoleId empty.", "100040"),
    CLASS_EMPTY("class empty.", "100041"),
    LOGISTICS_TYPE_ERROR("logistics_type not exist.", "100042"),
    ROLE_NAME_EMPTY("RoleName empty.", "100043"),
    USER_ID_EMPTY("UserId empty.", "100044"),
    PERMISSION_ID_EMPTY("PermissionId empty.", "100045"),
    PERMISSION_Type_EMPTY("Permission Type empty.", "100046"),
    PERMISSION_DESC_EMPTY("Permission Desc empty.", "100047"),
    DESC_C_EMPTY("desc_c empty.", "100055"),
    DESC_E_EMPTY("desc_e empty.", "100056"),
    PRICE_EMPTY("price empty.", "100057"),

    JOB_NAME_EMPTY("job name empty.", "100058"),


    ;

    private final String description;
    private final String code;

    private CheckErrorCode(String description, String code) {
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



