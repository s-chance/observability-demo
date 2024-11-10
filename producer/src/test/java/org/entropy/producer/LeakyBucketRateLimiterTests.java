package org.entropy.producer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeakyBucketRateLimiterTests {
    public static class LeakyBucketRateLimiter {
        private final long leakRate; // 漏水速率（单位时间内的请求数）
        private final long capacity; // 桶的容量
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicLong lastLeakTimestamp = new AtomicLong(System.nanoTime());
        private long availablePermits; // 可用令牌数

        public LeakyBucketRateLimiter(long leakRate, long capacity) {
            this.leakRate = leakRate;
            this.capacity = capacity;
            this.availablePermits = capacity; // 初始时桶满
        }

        public boolean tryAcquire() {
            long now = System.nanoTime();
            lock.lock();
            try {
                // 计算自上次漏水以来经过的时间
                long elapsed = now - lastLeakTimestamp.get();
                // 计算应该添加的令牌数
                long permitsToAdd = (elapsed * leakRate) / TimeUnit.SECONDS.toNanos(1);
                availablePermits = Math.min(capacity, availablePermits + permitsToAdd);

                if (availablePermits >= 1) {
                    availablePermits--;
                    lastLeakTimestamp.set(now);
                    return true;
                } else {
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }
    }


    @Test
    public void testLeakyBucketRateLimiter() throws InterruptedException {
        final int maxRequestsPerSecond = 5;
        final LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(maxRequestsPerSecond, 10);
        final int numberOfThreads = 20;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 创建并启动多个线程来模拟请求
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    if (limiter.tryAcquire()) {
                        successfulRequests.incrementAndGet(); // 记录成功的请求
                        System.out.println("Request successful, " + System.nanoTime());
                    } else {
                        System.out.println("Request failed, " + System.nanoTime());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown(); // 发送开始信号
        Thread.sleep(2000); // 等待2秒

        // 验证是否不超过maxRequestsPerSecond * 2个请求被允许
        System.out.println("Successful requests: " + successfulRequests.get());
        assertTrue(successfulRequests.get() <= maxRequestsPerSecond * 2);
    }
}
