package org.entropy.consumer;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RateLimitTests {

    @Test
    public void testRateLimit() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.create(5);

        for (int i = 0; i < 10; i++) {
            boolean b = rateLimiter.tryAcquire();
            TimeUnit.MILLISECONDS.sleep(100);
            if (b) {
                System.out.println(LocalDateTime.now() + ":success");
            } else {
                System.out.println(LocalDateTime.now() + ":fail");
            }
        }
    }
}
