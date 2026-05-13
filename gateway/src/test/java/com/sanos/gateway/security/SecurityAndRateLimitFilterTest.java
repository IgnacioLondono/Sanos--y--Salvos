package com.sanos.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para SecurityAndRateLimitFilter")
class SecurityAndRateLimitFilterTest {

    private SecurityAndRateLimitFilter filter;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private JwtValidator jwtValidator;

    @BeforeEach
    void setUp() {
        filter = new SecurityAndRateLimitFilter(jwtValidator);
    }

    @Test
    @DisplayName("Should allow public GET request without JWT")
    void shouldAllowPublicGetRequest() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/reports/list")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should allow public POST request (login)")
    void shouldAllowPublicPostRequest() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/iam/login")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should allow request to health check endpoints")
    void shouldAllowHealthCheckEndpoints() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/pets/health")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should allow request to OpenAPI documentation")
    void shouldAllowOpenAPIDocumentation() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/v3/api-docs")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should return forbidden for request without JWT")
    void shouldRejectRequestWithoutJWT() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/pets/create")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, chain))
                .expectComplete()
                .verify();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Filter order should be before other filters")
    void shouldHaveCorrectOrder() {
        // Act
        int order = filter.getOrder();

        // Assert
        assertEquals(-1, order);
    }
}
