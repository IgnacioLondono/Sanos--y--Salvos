package com.sanos.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecurityAndRateLimitFilter implements GlobalFilter, Ordered {

    /**
     * GET sin JWT: lectura pública para mapas, listados y coincidencias (escritura sigue protegida por método).
     */
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/reports",
            "/api/pets",
            "/api/matching",
            "/api/zones",
            "/api/media"
    );

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/iam/login",
            "/api/iam/register",
            "/v3/api-docs",
            "/openapi/",
            "/swagger-ui",
            "/swagger-ui.html",
            "/api/iam/health",
            "/api/pets/health",
            "/api/reports/health",
            "/api/zones/health",
            "/api/media/health",
            "/api/matching/health",
            "/api/capacity/health",
            "/api/audit/health",
            "/api/bff/health",
            "/actuator/health"
    );

    private static final int MAX_REQUESTS_PER_MINUTE = 240;
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<>();
    private final JwtValidator jwtValidator;

    public SecurityAndRateLimitFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        if (!allowRequest(clientIp)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        if (HttpMethod.GET.equals(request.getMethod()) && isPublicRead(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtValidator.parse(authHeader.substring(7));
            return chain.filter(exchange);
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean isPublicRead(String path) {
        return PUBLIC_GET_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean allowRequest(String clientIp) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        CounterWindow window = counters.computeIfAbsent(clientIp, k -> new CounterWindow(currentMinute, 0));
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
        return -1;
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
