package org.entropy.consumer.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.entropy.consumer.annotation.RateLimit;
import org.entropy.consumer.ratelimit.strategy.RateLimitStrategy;
import org.entropy.consumer.ratelimit.strategy.SlidingLogRateLimitStrategy;
import org.entropy.consumer.ratelimit.strategy.TokenBucketRateLimitStrategy;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, RateLimitStrategy> strategyCache = new ConcurrentHashMap<>();
    private RateLimitStrategy rateLimitStrategy;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
            if (rateLimit == null) {
                return true;
            }
            // 使用方法的签名作为键来获取或创建RateLimitStrategy实例
            String key = handlerMethod.getMethod().toString();
            RateLimitStrategy rateLimitStrategy = strategyCache.computeIfAbsent(key, k -> createRateLimitStrategy(rateLimit));
            if (!rateLimitStrategy.tryAcquire()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println("访问过于频繁");
                return false;
            }
            return true;
        }
        return true;
    }

    private RateLimitStrategy createRateLimitStrategy(RateLimit rateLimit) {
        return switch (rateLimit.strategy()) {
            case SLIDING_LOG ->
                    new SlidingLogRateLimitStrategy(rateLimit.maxPermits(), rateLimit.time(), rateLimit.timeUnit());
            case TOKEN_BUCKET ->
                    new TokenBucketRateLimitStrategy(rateLimit.maxPermits(), rateLimit.time(), rateLimit.timeUnit());
        };
    }
}
