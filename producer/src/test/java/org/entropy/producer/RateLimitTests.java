package org.entropy.producer;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimitTests {

    public static class FixedWindowRateLimiter {
        private long windowStart = System.currentTimeMillis();
        private int permits;
        private final int maxPermits;
        private final long windowSizeMillis;

        public FixedWindowRateLimiter(int maxPermits, long windowSizeMillis) {
            this.maxPermits = maxPermits;
            this.permits = maxPermits;
            this.windowSizeMillis = windowSizeMillis;
        }

        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now > windowStart + windowSizeMillis) {
                windowStart = now;
                permits = maxPermits;
            }
            if (permits > 0) {
                permits--;
                return true;
            }
            return false;
        }
    }

    @Test
    public void FixedWindowRateLimitTest() throws InterruptedException {
        final int maxPermits = 10;
        final long windowSizeMillis = 1000; // 1 seconds window
        final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(maxPermits, windowSizeMillis);
        final int numberOfThreads = 11;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 创建并启动多个线程来模拟请求
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    if (limiter.tryAcquire()) {
                        successfulRequests.incrementAndGet(); // 记录成功的请求
                        System.out.println("Request successful, " + LocalDateTime.now()); // 请求成功提示
                    } else {
                        System.out.println("Request limited," + LocalDateTime.now()); // 请求被限制提示
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown(); // 发送开始信号
        Thread.sleep(windowSizeMillis + 1000); // 等待窗口期结束

        // 验证是否至少有maxPermits个请求被允许
        System.out.println(successfulRequests.get());
        assertTrue(successfulRequests.get() >= maxPermits);
        // 验证是否不超过numberOfThreads个请求被允许
        assertTrue(successfulRequests.get() <= numberOfThreads);
    }
}
