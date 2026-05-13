package com.sanos.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RemoteServiceCallService {

    private final CircuitBreaker serviceCircuitBreaker;
    private final WebClient webClient;

    public RemoteServiceCallService(CircuitBreaker serviceCircuitBreaker, WebClient.Builder webClientBuilder) {
        this.serviceCircuitBreaker = serviceCircuitBreaker;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Realiza una llamada GET a un servicio remoto con protección de Circuit Breaker
     */
    public Mono<String> callRemoteService(String url) {
        return CircuitBreaker.decorateSupplier(
                serviceCircuitBreaker,
                () -> webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(e -> System.err.println("Error en llamada remota: " + e.getMessage()))
        ).get()
                .onErrorResume(throwable -> {
                    System.err.println("Circuit Breaker activado, devolviendo fallback");
                    return Mono.just("{\"error\": \"Servicio no disponible, intente más tarde\"}");
                });
    }

    /**
     * Realiza una llamada GET con parámetros
     */
    public Mono<String> callRemoteServiceWithParams(String baseUrl, String endpoint, String param1, String param2) {
        String url = String.format("%s/%s?param1=%s&param2=%s", baseUrl, endpoint, param1, param2);
        return callRemoteService(url);
    }

    /**
     * Obtiene el estado actual del Circuit Breaker
     */
    public String getCircuitBreakerStatus() {
        return String.format(
                "Estado: %s, Llamadas exitosas: %d, Fallos: %d",
                serviceCircuitBreaker.getState(),
                serviceCircuitBreaker.getMetrics().getNumberOfSuccessfulCalls(),
                serviceCircuitBreaker.getMetrics().getNumberOfFailedCalls()
        );
    }
}
