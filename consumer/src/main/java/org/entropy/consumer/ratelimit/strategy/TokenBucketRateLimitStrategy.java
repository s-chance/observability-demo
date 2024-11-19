package org.entropy.consumer.ratelimit.strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private final int maxTokens;
    private final long refillInterval;
    private final AtomicInteger availableTokens;
    private final AtomicLong lastRefillTimestamp;

    private final Thread refillThread;

    public TokenBucketRateLimitStrategy(int maxTokens, long refillInterval, TimeUnit unit) {
        this.maxTokens = maxTokens;
        this.refillInterval = unit.toMillis(refillInterval);
        this.availableTokens = new AtomicInteger(maxTokens);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());

        this.refillThread = new Thread(this::refillTokens);
        this.refillThread.setDaemon(true);
        this.refillThread.start();
    }

    private void refillTokens() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100); // 检查间隔，根据需要调整
                long now = System.currentTimeMillis();
                long lastRefill = lastRefillTimestamp.get();

                if (now > lastRefill) {
                    long elapsedTime = now - lastRefill;
                    long tokensToAdd = elapsedTime / refillInterval;

                    if (tokensToAdd > 0) {
                        // 尝试更新最后填充时间戳，如果成功则补充令牌
                        if (lastRefillTimestamp.compareAndSet(lastRefill, now)) {
                            int newTokens = Math.min(maxTokens, availableTokens.get() + (int) tokensToAdd);
                            availableTokens.set(newTokens);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public boolean tryAcquire() {
        return availableTokens.get() > 0 && availableTokens.decrementAndGet() >= 0;
    }
}
