package com.shang.distributelock.lock;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/20 11:15
 * @Description:
 */
@Service
public class RedisDistributedLocker implements DistributedLocker {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    /**
     * 加锁
     * @param lockKey key
     * @param leaseTime 持有锁的最长时间(单位默认是秒)
     */
    @Override
    public RLock lock(String lockKey, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime,TimeUnit.SECONDS);
        return lock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime,unit);
        return lock;
    }

    /**
     * 尝试加锁--出错会抛异常
     * @param lockKey key
     * @param unit 单位
     * @param waitTime 请求获取锁的最大超时时间
     * @param leaseTime 上锁后对锁的最长的持有时间
     * @return
     */
    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
           return lock.tryLock(waitTime,leaseTime,unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 解锁
     * @param lockKey key
     */
    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

    /**
     * 获取计数器
     *
     * @param name
     * @return
     */
    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return redissonClient.getCountDownLatch(name);
    }

    /**
     * 获取信号量
     *
     * @param name
     * @return
     */
    @Override
    public RSemaphore getSemaphore(String name) {
        return redissonClient.getSemaphore(name);
    }
}
