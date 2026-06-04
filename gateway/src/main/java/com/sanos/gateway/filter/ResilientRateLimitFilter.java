package com.sanos.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting por minuto (ventana deslizante por minuto calendario) + circuit breaker.
 */
@Component
public class ResilientRateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_MINUTE = 400;

    private final CircuitBreaker circuitBreaker;
    private final ConcurrentHashMap<String, CounterWindow> requestCounts = new ConcurrentHashMap<>();

    public ResilientRateLimitFilter(CircuitBreaker serviceCircuitBreaker) {
        this.circuitBreaker = serviceCircuitBreaker;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        String clientId = getClientIdentifier(exchange);
        if (!isWithinRateLimit(clientId)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String getClientIdentifier(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return "jwt:" + authorization.substring(7, Math.min(authorization.length(), 48));
        }

        return "ip:" + ip;
    }

    private boolean isWithinRateLimit(String clientId) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        CounterWindow window = requestCounts.computeIfAbsent(clientId, k -> new CounterWindow(currentMinute, 0));
        synchronized (window) {
            if (window.minute != currentMinute) {
                window.minute = currentMinute;
                window.count = 0;
            }
            window.count++;
            return window.count <= MAX_REQUESTS_PER_MINUTE;
        }
    }

    @Override
    public int getOrder() {
        return -50;
    }

    private static class CounterWindow {
        long minute;
        int count;

        CounterWindow(long minute, int count) {
            this.minute = minute;
            this.count = count;
        }
    }
}
