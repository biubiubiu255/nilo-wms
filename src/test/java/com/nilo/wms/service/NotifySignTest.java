package com.nilo.wms.service;

import com.nilo.wms.common.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/4/27.
 */
public class NotifySignTest {

    public static void main(String[] args) {

        String[] list = new String[]{
"1000000056811901"
        };

        for(String orderNo :list) {
            String url = "https://mobile.kilimall.co.ke/index.php?act=dms_api";
            String method = "confirmorder";
            String data = "{\"carrier\":\"Nilo\",\"consignee\":\"loise mghoi\",\"operateTime\":\"2018-04-11 12:29:15\",\"orderInfo\":[{\"orderNo\":" + orderNo + ",\"status\":\"10\"}],\"orderNo\":" + orderNo + ",\"orderPrice\":\"0\",\"remark\":\"loise mghoi\",\"rider\":\"40001\",\"transId\":\"\"}";
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
