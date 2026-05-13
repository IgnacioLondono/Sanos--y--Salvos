package com.sanos.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Filtro que combina Rate Limiting con Circuit Breaker
 */
@Component
public class ResilientRateLimitFilter implements GlobalFilter, Ordered {

    private final CircuitBreaker circuitBreaker;
    private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final long MAX_REQUESTS_PER_MINUTE = 100;

    public ResilientRateLimitFilter(CircuitBreaker serviceCircuitBreaker) {
        this.circuitBreaker = serviceCircuitBreaker;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Verificar si el circuito está abierto
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().writeWith(Mono.empty());
        }

        // Aplicar rate limiting
        String clientId = getClientIdentifier(exchange);
        if (!isWithinRateLimit(clientId)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().writeWith(Mono.empty());
        }

        return chain.filter(exchange);
    }

    /**
     * Obtiene un identificador único del cliente (IP o JWT)
     */
    private String getClientIdentifier(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7); // Usar el JWT como identificador
        }
        
        return ip;
    }

    /**
     * Verifica si el cliente está dentro del límite de rate
     */
    private boolean isWithinRateLimit(String clientId) {
        AtomicLong count = requestCounts.computeIfAbsent(clientId, k -> new AtomicLong(0));
        long currentCount = count.incrementAndGet();
        
        if (currentCount <= MAX_REQUESTS_PER_MINUTE) {
            return true;
        }
        
        // Resetear después de un minuto (simplificado)
        if (currentCount > MAX_REQUESTS_PER_MINUTE * 2) {
            requestCounts.remove(clientId);
            count.set(0);
        }
        
        return false;
    }

    @Override
    public int getOrder() {
        return -50; // Ejecutarse antes que otros filtros
    }
}

