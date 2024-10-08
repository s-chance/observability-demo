package org.entropy.gateway.filter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class OTelFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Context ctx = Context.current();
        Span span = Span.fromContext(ctx);
        if (span != null) {
            String traceId = span.getSpanContext().getTraceId();
            exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
