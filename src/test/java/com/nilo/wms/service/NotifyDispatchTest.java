package com.nilo.wms.service;

import com.nilo.wms.common.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/4/27.
 */
public class NotifyDispatchTest {

    public static void main(String[] args) {

        String[] list = new String[]{"10000000573450"
        };
        for(String orderNo:list) {

            String url = "https://mobile.kilimall.co.ke/index.php?act=wms_api";
            String method = "confirmSOData";
            String data = "{\"data\":{\"orderinfo\":[{\"CustomerID\":\"KILIMALL\",\"DeliveryNo\":\"KE10102410\",\"OrderNo\":" + orderNo + ",\"OrderType\":\"SELL\",\"Udf03\":\"Timothy Ngari\",\"Udf04\":\"0792001361\",\"Udf05\":\"Exdous\",\"Udf06\":\"0\",\"Udf08\":\"\",\"Udf09\":\"\",\"WarehouseID\":\"KE01\"}]}}";
            String sign = DigestUtils.md5Hex("12345678" + data + "12345678").toUpperCase();

            Map<String, String> param = new HashMap<>();
            param.put("data", URLEncoder.encode(data));
            param.put("sign", sign);
            param.put("op", method);
            try {
                String response = HttpUtil.post(url, param);

                System.out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
