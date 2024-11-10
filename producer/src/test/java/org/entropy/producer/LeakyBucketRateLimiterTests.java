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
        private long availableCapacity; // 可用容量

        public LeakyBucketRateLimiter(long leakRate, long capacity) {
            this.leakRate = leakRate;
            this.capacity = capacity;
            this.availableCapacity = capacity; // 初始时桶满
        }

        public boolean tryAcquire() {
            long now = System.nanoTime();
            lock.lock();
            try {
                // 计算自上次漏水以来经过的时间
                long elapsed = now - lastLeakTimestamp.get();
                // 计算应该添加的水量
                long capacityToAdd = (elapsed * leakRate) / TimeUnit.SECONDS.toNanos(1);
                availableCapacity = Math.min(capacity, availableCapacity + capacityToAdd);

                if (availableCapacity >= 1) {
                    availableCapacity--;
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
    public void testLeakyBucketRateLimiterByThread() throws InterruptedException {
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

    @Test
    public void testLeakyBucketRateLimiterByLoop() throws InterruptedException {
        final int maxRequestsPerSecond = 5;
        final LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(maxRequestsPerSecond, 10);
        final int numberOfThreads = 20;
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 模拟匀速请求
        for (int i = 0; i < numberOfThreads; i++) {
            TimeUnit.MILLISECONDS.sleep(100);
            if (limiter.tryAcquire()) {
                successfulRequests.incrementAndGet(); // 记录成功的请求
                System.out.println("Request successful, " + System.nanoTime());
            } else {
                System.out.println("Request failed, " + System.nanoTime());
            }
        }

        System.out.println("Successful requests: " + successfulRequests.get());
    }
}
