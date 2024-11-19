package org.entropy.consumer.annotation;

import org.entropy.consumer.enums.RateLimitStrategyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int maxPermits() default 10;

    long time() default 1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    RateLimitStrategyType strategy() default RateLimitStrategyType.TOKEN_BUCKET;
}
