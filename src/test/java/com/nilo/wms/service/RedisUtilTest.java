package com.nilo.wms.service;

import com.alibaba.fastjson.JSON;
import com.nilo.wms.common.Principal;
import com.nilo.wms.common.SessionLocal;
import com.nilo.wms.dto.StorageInfo;
import com.nilo.wms.dto.inbound.InboundHeader;
import com.nilo.wms.dto.outbound.OutboundHeader;
import com.nilo.wms.dto.platform.inbound.Inbound;
import com.nilo.wms.service.platform.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by admin on 2018/3/27.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:spring/*.xml"})
public class RedisUtilTest {

    @Autowired
    private BasicDataService basicDataService;

    //@Test
    public void testMultiLock() {

        String key_test = "test";
        // 使用线程安全的Vector
        Vector<Thread> threads = new Vector<Thread>();

        for (int i = 0; i < 200; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    Principal p = new Principal();
                    p.setClientCode("kilimall");
                    SessionLocal.setPrincipal(p);

                    String jsonStr = "{\"client_ordersn\":" + Thread.currentThread().getId() + ",\"order_items_list\":[{\"sku\":11680,\"goods_name\":\"Microphone\",\"goods_price\":\"16.07\",\"goods_num\":1},{\"sku\":11887,\"goods_name\":\"Microphone\",\"goods_price\":\"16.07\",\"goods_num\":1}]}";
                    OutboundHeader header = JSON.parseObject(jsonStr, OutboundHeader.class);
                    try {
                        List<StorageInfo> list = basicDataService.lockStorage(header);
                    } catch (Exception e) {
                        System.out.println(Thread.currentThread().getId());
                        e.printStackTrace();
                    }

                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread iThread : threads) {
            try {
                // 等待所有线程执行完毕
                iThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    //@Test
    public void testMultiUnLock() {

        String key_test = "test";
        // 使用线程安全的Vector
        Vector<Thread> threads = new Vector<Thread>();

        for (int i = 0; i < 200; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    Principal p = new Principal();
                    p.setClientCode("kilimall");
                    SessionLocal.setPrincipal(p);

                    String jsonStr = "{\"client_ordersn\":" + Thread.currentThread().getId() + ",\"order_items_list\":[{\"sku\":11680,\"goods_name\":\"Microphone\",\"goods_price\":\"16.07\",\"goods_num\":1},{\"sku\":11887,\"goods_name\":\"Microphone\",\"goods_price\":\"16.07\",\"goods_num\":1}]}";
                    OutboundHeader header = JSON.parseObject(jsonStr, OutboundHeader.class);
                    try {
                        basicDataService.unLockStorage(header.getOrderNo());
                    } catch (Exception e) {
                        System.out.println(Thread.currentThread().getId());
                        e.printStackTrace();
                    }

                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread iThread : threads) {
            try {
                // 等待所有线程执行完毕
                iThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
