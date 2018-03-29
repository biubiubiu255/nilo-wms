package com.nilo.wms.service;

import com.alibaba.fastjson.JSON;
import com.nilo.wms.dto.OutboundHeader;
import com.nilo.wms.dto.OutboundItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by admin on 2018/3/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/*.xml"})
public class BasicDataServiceTest {

    @Autowired
    private BasicDataService basicDataService;

    /*@Test*/
    public void lockStorage() {

        OutboundHeader header = new OutboundHeader();
        header.setOrderNo("1000000011");
        header.setCustomerId("kilimall");
        header.setWarehouseId("ke01");

        OutboundItem item1 = new OutboundItem();
        item1.setSku("1001");
        item1.setQty(2);

        OutboundItem item2 = new OutboundItem();
        item2.setSku("1002");
        item2.setQty(3);
        List<OutboundItem> list = new ArrayList<>();
        list.add(item1);
        list.add(item2);



        header.setItemList(list);

        System.out.println(JSON.toJSONString(header));

        RedisUtil.hset(RedisUtil.getSkuKey(header.getCustomerId(), header.getWarehouseId(), "1001"), RedisUtil.STORAGE, "10");
        RedisUtil.hset(RedisUtil.getSkuKey(header.getCustomerId(), header.getWarehouseId(), "1002"), RedisUtil.STORAGE, "10");

        basicDataService.lockStorage(header);

    }

    @Test
    public void multiLockStorage() {

        RedisUtil.hset(RedisUtil.getSkuKey("kilimall", "ke01", "1001"), RedisUtil.STORAGE, "4");
        RedisUtil.hset(RedisUtil.getSkuKey("kilimall", "ke01", "1002"), RedisUtil.STORAGE, "6");
        RedisUtil.hset(RedisUtil.getSkuKey("kilimall", "ke01", "1001"), RedisUtil.LOCK_STORAGE, "0");
        RedisUtil.hset(RedisUtil.getSkuKey("kilimall", "ke01", "1002"), RedisUtil.LOCK_STORAGE, "0");
        Vector<Thread> threads = new Vector<Thread>();

        for (int i = 0; i < 10; i++) {

            String no = "" + i;

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    OutboundHeader header = new OutboundHeader();
                    header.setOrderNo("10000000002" + no);
                    header.setCustomerId("kilimall");
                    header.setWarehouseId("ke01");

                    OutboundItem item1 = new OutboundItem();
                    item1.setSku("1001");
                    item1.setQty(3);

                    OutboundItem item2 = new OutboundItem();
                    item2.setSku("1002");
                    item2.setQty(3);
                    List<OutboundItem> list = new ArrayList<>();
                    list.add(item1);
                    list.add(item2);

                    header.setItemList(list);
                    try {
                        basicDataService.lockStorage(header);
                        System.out.println("Success==="+header.getOrderNo());
                    } catch (Exception e) {
                        System.out.println("Failed==="+header.getOrderNo());
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
        System.out.println(RedisUtil.hget(RedisUtil.getSkuKey("kilimall", "ke01", "1001"), RedisUtil.STORAGE));
        System.out.println(RedisUtil.hget(RedisUtil.getSkuKey("kilimall", "ke01", "1002"), RedisUtil.STORAGE));
        System.out.println(RedisUtil.hget(RedisUtil.getSkuKey("kilimall", "ke01", "1001"), RedisUtil.LOCK_STORAGE));
        System.out.println(RedisUtil.hget(RedisUtil.getSkuKey("kilimall", "ke01", "1002"), RedisUtil.LOCK_STORAGE));

    }


}
