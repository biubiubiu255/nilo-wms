
import com.nilo.wms.service.platform.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.Vector;

/**
 * Created by admin on 2018/3/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/*.xml"})
public class RedisUtilTest {


    @Test
    public void testMultiLock() {

        String key_test = "test";
        RedisUtil.set(key_test, "1");
        // 使用线程安全的Vector
        Vector<Thread> threads = new Vector<Thread>();
        System.out.println(System.currentTimeMillis());
        for (int i = 0; i < 200; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String key = "lock_lock";
                    String uuid = UUID.randomUUID().toString();
                    RedisUtil.tryGetDistributedLock("lock:test", uuid);
                    String value = RedisUtil.get(key_test);
                    try {
                        Thread.sleep(10 + (int) Math.random() * 10);
                        Integer val = Integer.parseInt(value) + 1;
                        RedisUtil.set(key_test, "" + val);
                        System.out.println(RedisUtil.get(key_test));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RedisUtil.releaseDistributedLock("lock:test", uuid);

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

        System.out.println(RedisUtil.get(key_test));
        System.out.println(System.currentTimeMillis());

    }
}
