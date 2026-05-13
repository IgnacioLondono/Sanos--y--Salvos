package com.sanos.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    /**
     * Configuración personalizada de Circuit Breaker
     * 
     * slidingWindowSize: Número de llamadas registradas
     * failureRateThreshold: Porcentaje de fallos para abrir el circuito
     * waitDurationInOpenState: Tiempo en OPEN antes de pasar a HALF_OPEN
     * permittedNumberOfCallsInHalfOpenState: Llamadas permitidas en HALF_OPEN
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        // Listener para monitorear eventos del circuito
        registry.getEventPublisher()
                .onEntryAdded(event -> System.out.println("CircuitBreaker creado: " + event.getAddedEntry().getName()))
                .onEntryRemoved(event -> System.out.println("CircuitBreaker removido: " + event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Circuit Breaker específico para servicios
     */
    @Bean
    public CircuitBreaker serviceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(15)
                .failureRateThreshold(60.0f)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return registry.circuitBreaker("serviceCircuitBreaker", config);
    }

    /**
     * Circuit Breaker para llamadas críticas (más sensible)
     */
    @Bean
    public CircuitBreaker criticalServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(8)
                .failureRateThreshold(30.0f)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        return registry.circuitBreaker("criticalServiceCircuitBreaker", config);
    }
}
