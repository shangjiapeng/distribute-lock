package com.shang.distributelock.redis;

import org.springframework.stereotype.Service;

/**
 * @Author: shangjp
 * @Email: shangjp@163.com
 * @Date: 2020/5/26 13:55
 * @Description: System.currentTimeMillis()消耗比较大, 可以在服务启动时,
 * 开启一个线程不断去拿,调用方法直接获取值
 */
@Service
public class TimeService {
    public static long time;

    static {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long cur = System.currentTimeMillis();
                setTime(cur);
            }
        }).start();

    }

    public static long getTime() {
        return time;
    }

    public static void setTime(long time) {
        TimeService.time = time;
    }
}
