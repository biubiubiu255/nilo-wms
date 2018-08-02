package com.nilo.wms.service.platform;

import com.nilo.wms.common.Principal;
import com.nilo.wms.common.exception.SysErrorCode;
import com.nilo.wms.common.exception.WMSException;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.Set;

/**
 * Created by admin on 2017/10/9.
 */
public class RedisUtil {

    public static final String STORAGE = "inventory";

    public static final String SAFE_STORAGE = "safeStorage";

    public static final String STORE = "store";

    public static final String LOCK_STORAGE = "lockStorage";

    public static final String LOCK_TIME = "lockTime";

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;
    public static final String LOCK_KEY = "wms_redis_lock_key";

    private static JedisPool jedisPool = SpringContext.getBean("jedisPool", JedisPool.class);

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否获取成功
     */
    public static void tryGetDistributedLock(String lockKey, String requestId) {

        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 0; i < 100; i++) {
                String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, 1000);
                if (LOCK_SUCCESS.equals(result)) {
                    return;
                } else {
                    Thread.yield();
                }
            }
        }
        throw new WMSException(SysErrorCode.SYSTEM_ERROR);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(String lockKey, String requestId) {

        try (Jedis jedis = jedisPool.getResource()) {

            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        }
    }

    public static void del(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public static void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public static void set(String key, String value, int second) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, second);
        }
    }

    public static String get(String key) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public static boolean hasKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public static void sAdd(String key, String[] value) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(key, value);
        }
    }

    public static void srem(String key, String[] value) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.srem(key, value);
        }
    }

    public static Set<String> sMember(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        }
    }

    public static boolean sExist(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(key, value);
        }
    }

    public static void hset(String key, String field, String value) {

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
        }
    }

    public static String hget(String key, String field) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    public static Set<String> hkeys(String key) {

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(key);
        }
    }

    public static long increment(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    public static Set<String> keys(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(key);
        }
    }

    public static String getSkuKey(String clientCode, String sku) {
        return "wms_" + clientCode + "_sku_" + sku;
    }

    public static String getLockOrderKey(String clientCode, String orderNo) {
        return "wms_" + clientCode + "_lock_order_" + orderNo;
    }

    public static String getRoleKey(String roleId) {
        return "wms_role_" + roleId;
    }

    public static String getUserKey(String userId) {
        return "wms_user_" + userId;
    }

    public static boolean hasPermission(String userId, String value) {
        return RedisUtil.sExist(RedisUtil.getRoleKey(RedisUtil.hget(RedisUtil.getUserKey(userId), "roleId")), value);
    }

    public static Principal getPrincipal(String userId) {
        Principal p = new Principal();
        String key = RedisUtil.getUserKey(userId);
        p.setRoleId(RedisUtil.hget(key, "roleId"));
        p.setUserId(userId);
        p.setWarehouseCode(RedisUtil.hget(key, "warehouseCode"));
        p.setCustomerId(RedisUtil.hget(key, "customerCode"));
        p.setWarehouseId(RedisUtil.hget(key, "warehouseCode"));
        p.setUserName(RedisUtil.hget(key, "userName"));
        p.setClientCode(RedisUtil.hget(key, "clientCode"));
        return p;
    }

}
