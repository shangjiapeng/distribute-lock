package com.shang.distributelock.lock;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;

import java.util.concurrent.TimeUnit;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/20 11:08
 * @Description: 底层封装
 */
public interface DistributedLocker {

    RLock lock(String lockKey);

    RLock lock(String lockKey, int timeout);

    RLock lock(String lockKey, TimeUnit unit, int timeout);

    boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime);

    void unlock(String lockKey);

    void unlock(RLock lock);

    /**
     * 获取计数器
     *
     * @param name
     * @return
     */
    RCountDownLatch getCountDownLatch(String name);

    /**
     * 获取信号量
     *
     * @param name
     * @return
     */
    RSemaphore getSemaphore(String name);

}
