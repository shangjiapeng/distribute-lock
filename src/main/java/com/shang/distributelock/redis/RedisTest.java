package com.shang.distributelock.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/26 11:27
 * @Description:
 */
public class RedisTest {
    //库存
    private static Integer inventory = 1001;
    //并发线程的数量
    private static final int NUM = 1000;
    //任务队列
    private static LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
    //可重入锁
    static ReentrantLock reentrantLock = new ReentrantLock();

    static RedisLock redisLock = new RedisLock();

    public static void main(String[] args) {
        test1();
    }

    /**
     * 创建很多的线程去同时扣减库存,不加锁的情况加,结果是不对的
     */
    private static void test1() {
        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingQueue);
        //创建一个栅栏
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        //记录开始时间
        long start = System.currentTimeMillis();
        //模拟并发请求
        for (int i = 0; i <= NUM; i++) {
            threadPoolExecutor.execute(() -> {
                inventory--;
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                countDownLatch.countDown();
            });
        }
        //使用完毕之后,关闭线程池
        threadPoolExecutor.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行的总线程数:" + NUM + " 总耗时:" + (end - start) + " 库存数为:" + inventory);
    }

    /**
     * 单价情况下,加synchronize或者Lock即可实现保证结果正确
     */
    private static void test2() {
        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingQueue);
        //创建一个栅栏
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        //记录开始时间
        long start = System.currentTimeMillis();
        //模拟并发请求
        for (int i = 0; i <= NUM; i++) {
            threadPoolExecutor.execute(() -> {
                reentrantLock.lock();
                inventory--;
                reentrantLock.unlock();
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                countDownLatch.countDown();
            });
        }
        //使用完毕之后,关闭线程池
        threadPoolExecutor.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行的总线程数:" + NUM + " 总耗时:" + (end - start) + " 库存数为:" + inventory);
    }

    /**
     * 使用自定义分布式锁扣减库存
     */
    private static void test3() {
        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingQueue);
        //创建一个栅栏
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        //记录开始时间
        long start = System.currentTimeMillis();
        //模拟并发请求
        for (int i = 0; i <= NUM; i++) {
            threadPoolExecutor.execute(() -> {
                redisLock.lock(UUID.randomUUID().toString());
                inventory--;
                redisLock.unlock(UUID.randomUUID().toString());
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                countDownLatch.countDown();
            });
        }
        //使用完毕之后,关闭线程池
        threadPoolExecutor.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行的总线程数:" + NUM + " 总耗时:" + (end - start) + " 库存数为:" + inventory);
    }

    /**
     * 使用Redisson实现可重入的分布式锁
     */
    private static void test4() {
        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                inventory, inventory, 10L, TimeUnit.SECONDS, linkedBlockingQueue);
        //创建一个栅栏
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        //记录开始时间
        long start = System.currentTimeMillis();

        //获取redisson分布式锁
        Config redissonConfig = new Config();
        redissonConfig.useSingleServer().setAddress("127.0.0.1:6379");
        final RedissonClient redissonClient = Redisson.create(redissonConfig);
        final RLock lock1 = redissonClient.getLock("lock1");

        //模拟并发请求
        for (int i = 0; i <= NUM; i++) {
            threadPoolExecutor.execute(() -> {
                lock1.lock();
                inventory--;
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                lock1.unlock();
                countDownLatch.countDown();
            });
        }
        //使用完毕之后,关闭线程池
        threadPoolExecutor.shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行的总线程数:" + NUM + " 总耗时:" + (end - start) + " 库存数为:" + inventory);
    }



}
