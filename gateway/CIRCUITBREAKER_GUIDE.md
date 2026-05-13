# Circuit Breaker & Pruebas Unitarias - Gateway

## Descripcion

Este documento describe cómo integrar y usar **Circuit Breaker** con **Resilience4j** y cómo ejecutar **pruebas unitarias** en el gateway.

---

##  Configuración de Circuit Breaker

### ¿Qué es Circuit Breaker?

Circuit Breaker es un patrón de diseño que previene llamadas a servicios que están fallando. Funciona como un interruptor eléctrico con 3 estados:

- **CLOSED**: Operación normal, todas las llamadas se ejecutan
- **OPEN**: Servicio no disponible, rechaza inmediatamente sin llamar
- **HALF_OPEN**: Permite N llamadas para verificar si el servicio se recuperó

### Estados y Transiciones

```
CLOSED --failureRate > threshold--> OPEN --waitDuration--> HALF_OPEN --success/failure--> CLOSED/OPEN
```

### Configuración en `application.yml`

```yaml
resilience4j:
  circuitbreaker:
    instances:
      serviceCircuitBreaker:
        slidingWindowSize: 10              # Últimas 10 llamadas a considerar
        failureRateThreshold: 50           # % de fallos para abrir circuito
        waitDurationInOpenState: 30s       # Tiempo antes de pasar a HALF_OPEN
        permittedNumberOfCallsInHalfOpenState: 3  # Llamadas para verificar recuperación
```

---

##  Uso en el Código

### 1. Inyectar Circuit Breaker en un servicio

```java
@Service
public class MyService {
    
    private final CircuitBreaker circuitBreaker;
    
    public MyService(CircuitBreaker serviceCircuitBreaker) {
        this.circuitBreaker = serviceCircuitBreaker;
    }
    
    public Mono<String> callRemoteApi() {
        return CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> webClient.get()
                .uri("http://remote-service/api")
                .retrieve()
                .bodyToMono(String.class)
        ).get()
        .onErrorResume(throwable -> {
            log.error("Fallback: servicio no disponible");
            return Mono.just("{\"error\": \"Servicio no disponible\"}");
        });
    }
}
```

### 2. Monitorear el estado del Circuit Breaker

```java
String status = circuitBreaker.getMetrics()
    .getState() // CLOSED, OPEN, HALF_OPEN
    .toString();

int successfulCalls = circuitBreaker.getMetrics()
    .getNumberOfSuccessfulCalls();

int failedCalls = circuitBreaker.getMetrics()
    .getNumberOfFailedCalls();
```

---

##  Pruebas Unitarias

### Dependencias incluidas

- **Mockito 5.7.0**: Para mockar objetos
- **Reactor Test**: Para pruebas reactivas
- **Spring Boot Test**: Framework de testing

### Archivos de prueba incluidos

1. **RemoteServiceCallServiceTest.java**
   - Prueba llamadas con Circuit Breaker
   - Prueba fallback cuando circuito está abierto
   - Prueba métricas del circuito

2. **SecurityAndRateLimitFilterTest.java**
   - Prueba autorización sin JWT
   - Prueba endpoints públicos
   - Prueba rate limiting

### Ejecutar pruebas

```bash
# Todas las pruebas
mvn test

# Pruebas de un archivo específico
mvn test -Dtest=RemoteServiceCallServiceTest

# Con cobertura
mvn test jacoco:report

# Solo pruebas que contienen "CircuitBreaker"
mvn test -Dtest=*CircuitBreaker*
```

### Estructura de una prueba

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    @Mock
    private CircuitBreaker mockCircuitBreaker;
    
    @BeforeEach
    void setUp() {
        // Configuración inicial
    }
    
    @Test
    @DisplayName("Should handle service failure")
    void shouldHandleFailure() {
        // Arrange: preparar datos
        when(mockCircuitBreaker.getState())
            .thenReturn(CircuitBreaker.State.OPEN);
        
        // Act: ejecutar código
        Mono<String> result = service.callApi();
        
        // Assert: verificar resultado
        StepVerifier.create(result)
            .expectNextMatches(r -> r.contains("error"))
            .verifyComplete();
    }
}
```

---

##  Monitoreo y Métricas

### Endpoints disponibles

```bash
# Health del gateway
curl http://localhost:8080/actuator/health

# Detalle de métricas (si se configura)
curl http://localhost:8080/actuator/metrics

# Circuit breaker específico
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

### Logs del Circuit Breaker

En `application.yml` puedes agregar:

```yaml
logging:
  level:
    io.github.resilience4j: DEBUG
    com.sanos.gateway: DEBUG
```

---

##  Mejores Prácticas

###  Hacer

-  Usar diferentes CircuitBreakers para diferentes tipos de servicios
-  Configurar timeouts apropiados según el servicio
-  Monitorear métricas en producción
-  Proporcionar fallbacks útiles (no solo errores)
-  Probar tanto casos exitosos como fallos en tus unit tests

###  NO hacer

-  Usar un solo CircuitBreaker para todos los servicios
-  Configurar thresholds muy bajos (causaría circuit opens frecuentes)
-  Ignorar fallbacks
-  Pruebas sin mocks (pueden fallar por dependencias externas)
-  Cambiar configuración en tiempo de ejecución sin validar

---

##  Ejemplo Completo

Ver archivos:
- [CircuitBreakerConfig.java](src/main/java/com/sanos/gateway/config/CircuitBreakerConfig.java)
- [RemoteServiceCallService.java](src/main/java/com/sanos/gateway/service/RemoteServiceCallService.java)
- [RemoteServiceCallServiceTest.java](src/test/java/com/sanos/gateway/service/RemoteServiceCallServiceTest.java)

---

##  Referencias

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
