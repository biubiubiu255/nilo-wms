package com.nilo.wms.service;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author ronny.zeng
 * @date 2018/6/21.
 */
public class Test {
    public static void main(String[] args) {

        String data="{\"add_time\":1529393702,\"alreadyPaid\":1650,\"carrier_name\":\"NBO\",\"channelStation\":\"\",\"client_name\":\"kilimall\",\"client_order_sn\":\"10000000594334\",\"delivery_type\":\"0\",\"goods_type_id\":\"0\",\"is_pod\":\"1\",\"logistics_type\":\"1\",\"need_pay_amount\":0,\"orderCategory\":\"FBK\",\"order_amount\":2535,\"order_items_list\":[{\"goods_name\":\"3pcs Ethnic Style Girl Geometric Dot Print Portable Handbag Tote School Backpack gray one size\",\"goods_num\":1,\"goods_price\":2635.00,\"sku\":\"781388\"}],\"receiver_info\":{\"city\":\"Westlands\",\"contact_name\":\"akshi tank\",\"contact_number\":\" 254725258888\",\"receiver_address\":\"Nairobi,Westlands Chemilil Road, Ngara Opp St Ann School and Next to Marigold Apartment\"},\"sender_info\":{},\"stop\":\"\",\"waybill_number\":\"KE30012518\"}";

        System.out.println(DigestUtils.md5Hex("12345678"+data+"12345678"));

    }
}
