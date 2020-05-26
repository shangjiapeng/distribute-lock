package com.shang.distributelock.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/26 13:17
 * @Description: redis 锁
 */
public class RedisLock {
    //库存
    private static Integer inventory = 1001;
    //线程数量
    private static final int NUM = 1000;
    //key
    private String LOCK_KEY = "redis_lock";
    //自动失效时间
    protected long INTERNAL_LOCK_LEASE_TIME = 3;
    //超时时间,超过后自动退出
    private long timeout = 1000;
    //nx px 命令的集合
    private SetParams params = SetParams.setParams().nx().px(INTERNAL_LOCK_LEASE_TIME);

    private static Jedis jedis = new JedisPool("127.0.0.1", 6379).getResource();


    public RedisLock() {
    }

    public static void main(String[] args) {

        inventory = Integer.parseInt(jedis.get("inventory"));
        try {
            for (int i = 0; i < NUM; i++) {
                new Thread(() -> {
                    try {
                        jedis.setnx("lock", "test");
                        Thread.sleep(1);
                        if (RedisLock.inventory > 0) {
                            RedisLock.inventory--;
                        }
                        System.out.println(RedisLock.inventory);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } finally {
            jedis.close();
        }
    }

    /**
     * 加锁
     */
    public boolean lock(String id) {
        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < NUM; i++) {
                //set命令返回OK 则证明获取锁成功
                String lock = jedis.set(LOCK_KEY, id, params);
                if ("OK".equals(lock)) {
                    return true;
                }
                //否则循环等待,在timeout时间内仍未获取到锁,则获取锁失败
                long time = System.currentTimeMillis() - start;
                if (time > timeout) {
                    return false;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        } finally {
            jedis.close();
        }
        return false;
    }

    /**
     * 解锁
     * 解锁的逻辑比较简单,就是一点lua的拼装,把key做了删除
     * 使用UUID是为了保证谁加的所,谁解锁
     * lua是原子性的: 就是判断key与我们的参数是否相等,是的话就删除,返回成功1 ,失败返回0
     */
    public boolean unlock(String id) {
        String luaScript =
                "if redis.call('get',KEYS[1])==ARGV[1] then " +
                        "   return redis.call('del',KEYS[1]) " +
                        "else" +
                        "   return 0 " +
                        "end ";
        try {
            String result=jedis.eval(luaScript, Collections.singletonList(LOCK_KEY),Collections.singletonList(id)).toString();
            return "1".equals(result);
        }finally {
            jedis.close();
        }
    }


}
