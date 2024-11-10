package org.entropy.producer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowRateLimiterTests {

    public static class SlidingWindowRateLimiter {
        private final ConcurrentLinkedQueue<Long> timestamps = new ConcurrentLinkedQueue<>();
        private final int maxPermits;
        private final long windowSizeMillis;

        public SlidingWindowRateLimiter(int maxPermits, long windowSizeMillis) {
            this.maxPermits = maxPermits;
            this.windowSizeMillis = windowSizeMillis;
        }

        public AtomicBoolean tryAcquire() {
            synchronized (timestamps) {
                long now = System.currentTimeMillis();
                // 移除窗口之外的时间戳
                while (!timestamps.isEmpty() && (now - timestamps.peek() > windowSizeMillis)) {
                    timestamps.poll();
                }

                // 检查是否超出限制
                if (timestamps.size() < maxPermits) {
                    timestamps.offer(now);
                    return new AtomicBoolean(true);
                } else {
                    return new AtomicBoolean(false);
                }
            }
        }
    }

    @Test
    public void testSlidingWindowRateLimiterByThread() throws InterruptedException {
        final int maxPermits = 10;
        final long windowSizeMillis = 1000; // 1 seconds window
        final SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(maxPermits, windowSizeMillis);
        final int numberOfThreads = 11;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 创建并启动多个线程来模拟请求
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    if (limiter.tryAcquire().get()) {
                        successfulRequests.incrementAndGet(); // 记录成功的请求
                        System.out.println("Request successful, " + System.currentTimeMillis());
                    } else {
                        System.out.println("Request limited, " + System.currentTimeMillis());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown(); // 发送开始信号
        Thread.sleep(windowSizeMillis + 100); // 等待窗口期结束

        // 验证是否至少有maxPermits个请求被允许
        System.out.println("Successful requests: " + successfulRequests.get());
        assertTrue(successfulRequests.get() >= maxPermits);
        // 验证是否不超过numberOfThreads个请求被允许
        assertTrue(successfulRequests.get() <= numberOfThreads);
    }

    @Test
    public void testSlidingWindowRateLimiterByLoop() throws InterruptedException {
        final int maxPermits = 10;
        final long windowSizeMillis = 1000; // 1 seconds window
        final SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(maxPermits, windowSizeMillis);
        final int numberOfThreads = 11;
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 模拟匀速请求
        for (int i = 0; i < numberOfThreads; i++) {
            TimeUnit.MILLISECONDS.sleep(50);
            if (limiter.tryAcquire().get()) {
                successfulRequests.incrementAndGet(); // 记录成功的请求
                System.out.println("Request successful, " + System.currentTimeMillis());
            } else {
                System.out.println("Request limited, " + System.currentTimeMillis());
            }
        }

        System.out.println("Successful requests: " + successfulRequests.get());
    }
}
