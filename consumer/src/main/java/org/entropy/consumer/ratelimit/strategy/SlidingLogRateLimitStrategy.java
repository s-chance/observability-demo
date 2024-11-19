package org.entropy.consumer.ratelimit.strategy;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class SlidingLogRateLimitStrategy implements RateLimitStrategy {
    private final int maxPermits;
    private final long timeWindow;
    private final ConcurrentLinkedQueue<Long> timestamps;

    public SlidingLogRateLimitStrategy(int maxPermits, long timeWindow, TimeUnit unit) {
        this.maxPermits = maxPermits;
        this.timeWindow = unit.toMillis(timeWindow);
        this.timestamps = new ConcurrentLinkedQueue<>();
    }

    @Override
    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        while (!timestamps.isEmpty() && now - timestamps.peek() > timeWindow) {
            timestamps.poll();
        }
        if (timestamps.size() < maxPermits) {
            timestamps.add(now);
            return true;
        }
        return false;
    }
}
