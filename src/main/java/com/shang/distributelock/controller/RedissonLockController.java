package com.shang.distributelock.controller;

import com.shang.distributelock.utis.RedisLockUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/20 15:29
 * @Description: redis分布式锁控制器
 */
@RestController
@Api(tags = "redisson", description = "redis分布式锁控制器")
@RequestMapping("/redisson")
@Slf4j
public class RedissonLockController {

    /**
     * 锁测试共享变量
     */
    private Integer lockCount = 10;

    /**
     * 无锁测试共享变量
     */
    private Integer count = 10;

    /**
     * 模拟线程数
     */
    private static int threadNum =10;

    /**
     * 模拟并发测试加锁和不加锁
     * 根据打印结果可以明显看到，未加锁的 count-- 后值是乱序的，而加锁后的结果和我们预期的一样。
     * 由于条件问题没办法测试分布式的并发。只能模拟单服务的这种并发，但是原理是一样，
     * @return
     */
    @GetMapping("/test")
    @ApiOperation("模拟并发测试加锁和不加锁")
    private void lock() {
        //计数器
        final CountDownLatch countDownLatch =new CountDownLatch(1);

        for (int i = 0; i < threadNum; i++) {

            MyRunnable myRunnable = new MyRunnable(countDownLatch);
            Thread thread = new Thread(myRunnable);
            thread.start();
        }
        //释放所有的线程
        countDownLatch.countDown();
    }

    /**
     * 加锁测试
     */
    private  void  testLockCount() {
        String lockKey = "lock-test";
        try {
            RedisLockUtil.lock(lockKey,2, TimeUnit.SECONDS);
            lockCount--;
            log.info("lockCount值:"+lockCount);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }finally {
            //必须在finally代码块中释放锁,避免产生死锁
            RedisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 无锁测试
     */
    private void testCount() {
        count--;
        log.info("count值:"+count);
    }


    public class MyRunnable implements Runnable {

        /**
         * 计数器
         */
        final CountDownLatch countDownLatch;

        public MyRunnable(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }


        @Override
        public void run() {
            //阻塞当前线程,直到计数器的值为0
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }
            //无锁操作
            testCount();

            //加锁操作
            testLockCount();
        }
    }












}
