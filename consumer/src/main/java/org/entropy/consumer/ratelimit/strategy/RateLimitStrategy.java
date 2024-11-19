package org.entropy.consumer.ratelimit.strategy;

public interface RateLimitStrategy {
    boolean tryAcquire();
}
