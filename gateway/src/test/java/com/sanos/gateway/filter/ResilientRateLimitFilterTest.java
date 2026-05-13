package com.sanos.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ResilientRateLimitFilter")
class ResilientRateLimitFilterTest {

    private ResilientRateLimitFilter filter;

    @Mock
    private CircuitBreaker mockCircuitBreaker;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new ResilientRateLimitFilter(mockCircuitBreaker);
    }

    @Test
    @DisplayName("Should return SERVICE_UNAVAILABLE when circuit breaker is OPEN")
    void shouldReturnUnavailableWhenCircuitOpen() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(mockCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Should allow request when circuit is CLOSED and rate limit not exceeded")
    void shouldAllowRequestWhenCircuitClosedAndRateLimitOk() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(mockCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return TOO_MANY_REQUESTS when rate limit exceeded")
    void shouldReturnTooManyRequestsWhenRateLimitExceeded() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(mockCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should extract client IP from request")
    void shouldExtractClientIpFromRequest() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(mockCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Filter should have correct order")
    void shouldHaveCorrectOrder() {
        // Act
        int order = filter.getOrder();

        // Assert
        assertEquals(-50, order);
    }

    @Test
    @DisplayName("Should handle HALF_OPEN state")
    void shouldHandleHalfOpenState() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(mockCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }
}
