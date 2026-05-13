package com.sanos.gateway.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para monitorear el estado del Circuit Breaker
 */
@RestController
@RequestMapping("/api/gateway/circuit-breaker")
public class CircuitBreakerMonitorController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CircuitBreaker serviceCircuitBreaker;
    private final CircuitBreaker criticalServiceCircuitBreaker;

    public CircuitBreakerMonitorController(
            CircuitBreakerRegistry circuitBreakerRegistry,
            CircuitBreaker serviceCircuitBreaker,
            CircuitBreaker criticalServiceCircuitBreaker) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.serviceCircuitBreaker = serviceCircuitBreaker;
        this.criticalServiceCircuitBreaker = criticalServiceCircuitBreaker;
    }

    /**
     * GET /api/gateway/circuit-breaker/status
     * Obtiene el estado de todos los Circuit Breakers
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getAllStatus() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            
            response.put("serviceCircuitBreaker", getCircuitBreakerDetails(serviceCircuitBreaker));
            response.put("criticalServiceCircuitBreaker", getCircuitBreakerDetails(criticalServiceCircuitBreaker));
            response.put("totalCircuitBreakers", circuitBreakerRegistry.getAllCircuitBreakers().size());
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * GET /api/gateway/circuit-breaker/{name}
     * Obtiene detalles de un Circuit Breaker específico
     */
    @GetMapping("/{name}")
    public Mono<ResponseEntity<Map<String, Object>>> getCircuitBreakerStatus(@PathVariable String name) {
        return Mono.fromCallable(() -> {
            CircuitBreaker cb = null;
            
            if ("serviceCircuitBreaker".equals(name)) {
                cb = serviceCircuitBreaker;
            } else if ("criticalServiceCircuitBreaker".equals(name)) {
                cb = criticalServiceCircuitBreaker;
            }
            
            if (cb == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(getCircuitBreakerDetails(cb));
        });
    }

    /**
     * GET /api/gateway/circuit-breaker/health/detailed
     * Obtiene información detallada para health checks
     */
    @GetMapping("/health/detailed")
    public Mono<ResponseEntity<Map<String, Object>>> getDetailedHealth() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            
            Map<String, Object> service = getCircuitBreakerDetails(serviceCircuitBreaker);
            Map<String, Object> critical = getCircuitBreakerDetails(criticalServiceCircuitBreaker);
            
            // Agregar información de salud
            boolean isHealthy = !service.get("state").equals("OPEN") && 
                               !critical.get("state").equals("OPEN");
            
            response.put("healthy", isHealthy);
            response.put("circuitBreakers", Map.of(
                "serviceCircuitBreaker", service,
                "criticalServiceCircuitBreaker", critical
            ));
            
            return ResponseEntity.ok(response);
        });
    }

    /**
     * Obtiene detalles completos de un Circuit Breaker
     */
    private Map<String, Object> getCircuitBreakerDetails(CircuitBreaker cb) {
        Map<String, Object> details = new HashMap<>();
        CircuitBreaker.State state = cb.getState();
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        
        details.put("name", cb.getName());
        details.put("state", state.toString()); // CLOSED, OPEN, HALF_OPEN
        details.put("stateDescription", getStateDescription(state));
        
        // Métricas
        long successfulCalls = metrics.getNumberOfSuccessfulCalls();
        long failedCalls = metrics.getNumberOfFailedCalls();
        long slowCalls = metrics.getNumberOfSlowCalls();
        long bufferedCalls = metrics.getNumberOfBufferedCalls();
        
        double successRate = bufferedCalls > 0 ? (successfulCalls * 100.0) / bufferedCalls : 0.0;
        double slowCallRate = bufferedCalls > 0 ? (slowCalls * 100.0) / bufferedCalls : 0.0;
        
        Map<String, Object> metricsMap = new HashMap<>();
        metricsMap.put("successfulCalls", successfulCalls);
        metricsMap.put("failedCalls", failedCalls);
        metricsMap.put("slowCalls", slowCalls);
        metricsMap.put("bufferedCalls", bufferedCalls);
        metricsMap.put("successRate", String.format("%.2f%%", successRate));
        metricsMap.put("slowCallRate", String.format("%.2f%%", slowCallRate));
        
        details.put("metrics", metricsMap);
        
        // Config
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("failureRateThreshold", cb.getCircuitBreakerConfig().getFailureRateThreshold());
        configMap.put("slowCallRateThreshold", cb.getCircuitBreakerConfig().getSlowCallRateThreshold());
        configMap.put("slidingWindowSize", cb.getCircuitBreakerConfig().getSlidingWindowSize());
        configMap.put("permittedCallsInHalfOpen", cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
        
        details.put("config", configMap);
        
        return details;
    }

    /**
     * Descripción amigable del estado
     */
    private String getStateDescription(CircuitBreaker.State state) {
        return switch (state) {
            case CLOSED -> "Operacion normal - Todas las llamadas se procesan";
            case OPEN -> "Circuito abierto - Rechaza llamadas, servicio no disponible";
            case HALF_OPEN -> "Verificando recuperacion - Permitidas llamadas limitadas";
            default -> "Estado desconocido";
        };
    }
}
