package org.entropy.producer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenBucketRateLimiterTest {
    public static class TokenBucketRateLimiter {
        private final long maxTokens; // 桶的最大容量
        private final long refillRate; // 令牌填充速率（每秒添加的令牌数）
        private final long refillInterval; // 令牌填充的时间间隔（纳秒）
        private final AtomicLong availableTokens; // 当前可用的令牌数
        private final AtomicLong lastRefillTimestamp; // 上次填充令牌的时间戳（纳秒）

        public TokenBucketRateLimiter(long maxTokens, long refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.refillInterval = TimeUnit.SECONDS.toNanos(1) / refillRate;
            this.availableTokens = new AtomicLong(maxTokens);
            this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
        }

        public boolean tryAcquire() {
            long now = System.nanoTime();
            long tokensToAdd = (now - lastRefillTimestamp.get()) / refillInterval;
            if (tokensToAdd > 0) {
                long newTokens = Math.min(maxTokens, availableTokens.get() + tokensToAdd);
                availableTokens.set(newTokens);
                lastRefillTimestamp.set(now);
            }
            return availableTokens.getAndDecrement() > 0;
        }
    }

    @Test
    public void testTokenBucketRateLimiterByThread() throws InterruptedException {
        final int maxPermits = 10;
        final long permitsPerSecond = 5;
        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(maxPermits, permitsPerSecond);
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
                        System.out.println("Request limited, " + System.nanoTime());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown(); // 发送开始信号
        Thread.sleep(2000); // 等待2秒

        // 验证是否至少有maxPermits个请求被允许
        System.out.println("Successful requests: " + successfulRequests.get());
        assertTrue(successfulRequests.get() >= maxPermits);
        // 验证是否不超过numberOfThreads个请求被允许
        assertTrue(successfulRequests.get() <= numberOfThreads);
    }

    @Test
    public void testTokenBucketRateLimiterByLoop() throws InterruptedException {
        final int maxPermits = 10;
        final long permitsPerSecond = 5;
        final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(maxPermits, permitsPerSecond);
        final int numberOfThreads = 20;
        final AtomicInteger successfulRequests = new AtomicInteger(0);

        // 模拟匀速请求
        for (int i = 0; i < numberOfThreads; i++) {
            TimeUnit.MILLISECONDS.sleep(100);
            if (limiter.tryAcquire()) {
                successfulRequests.incrementAndGet(); // 记录成功的请求
                System.out.println("Request successful, " + System.nanoTime());
            } else {
                System.out.println("Request limited, " + System.nanoTime());
            }
        }

        System.out.println("Successful requests: " + successfulRequests.get());
    }
}
