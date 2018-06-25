package com.nilo.wms.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/9.
 */
public enum MoneyType {

    In_Bound("1", "收货上架"),

    Order_Handler("2", "库内操作"),

    Storage("3", "库存"),

    Order_Return("4", "退货"),

    Return_Merchant("5", "返厂处理");



    private String code;

    private String desc;
    private static Map<String, MoneyType> map = new HashMap<String, MoneyType>(10);

    static {
        MoneyType[] enums = MoneyType.values();
        for (MoneyType e : enums) {
            map.put(e.getCode(), e);
        }
    }

    public static MoneyType getEnum(String code) {
        return map.get(code);
    }
    private MoneyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     *
     * @param code value to be assigned to property code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     *
     * @return property value of desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc</tt>.
     *
     * @param desc value to be assigned to property desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
