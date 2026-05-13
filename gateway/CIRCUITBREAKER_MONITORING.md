# Monitoreo del Circuit Breaker

## Endpoints Disponibles

Después de iniciar el gateway en `http://localhost:8080`, puedes acceder a estos endpoints:

---

## 1. Estado General del Circuit Breaker

**Endpoint:**
```
GET http://localhost:8080/api/gateway/circuit-breaker/status
```

**Ejemplo con curl:**
```bash
curl -X GET http://localhost:8080/api/gateway/circuit-breaker/status | jq
```

**Respuesta esperada:**
```json
{
  "serviceCircuitBreaker": {
    "name": "serviceCircuitBreaker",
    "state": "CLOSED",
    "stateDescription": " Operación normal - Todas las llamadas se procesan",
    "metrics": {
      "successfulCalls": 45,
      "failedCalls": 2,
      "slowCalls": 1,
      "bufferedCalls": 48,
      "successRate": "95.74%",
      "slowCallRate": "2.13%"
    },
    "config": {
      "failureRateThreshold": 60.0,
      "slowCallRateThreshold": 50.0,
      "waitDurationInOpenState": "PT20S",
      "permittedCallsInHalfOpen": 5
    }
  },
  "criticalServiceCircuitBreaker": {
    "name": "criticalServiceCircuitBreaker",
    "state": "CLOSED",
    "stateDescription": " Operación normal - Todas las llamadas se procesan",
    "metrics": {
      "successfulCalls": 20,
      "failedCalls": 0,
      "slowCalls": 0,
      "bufferedCalls": 20,
      "successRate": "100.00%",
      "slowCallRate": "0.00%"
    },
    "config": {
      "failureRateThreshold": 30.0,
      "slowCallRateThreshold": 40.0,
      "waitDurationInOpenState": "PT45S",
      "permittedCallsInHalfOpen": 2
    }
  },
  "totalCircuitBreakers": 2
}
```

---

## 2. Estado de un Circuit Breaker Especifico

**Endpoint:**
```
GET http://localhost:8080/api/gateway/circuit-breaker/{name}
```

**Ejemplo 1 - Service Circuit Breaker:**
```bash
curl -X GET http://localhost:8080/api/gateway/circuit-breaker/serviceCircuitBreaker | jq
```

**Ejemplo 2 - Critical Service Circuit Breaker:**
```bash
curl -X GET http://localhost:8080/api/gateway/circuit-breaker/criticalServiceCircuitBreaker | jq
```

---

## 3. Health Check Detallado

**Endpoint:**
```
GET http://localhost:8080/api/gateway/circuit-breaker/health/detailed
```

**Ejemplo:**
```bash
curl -X GET http://localhost:8080/api/gateway/circuit-breaker/health/detailed | jq
```

**Respuesta:**
```json
{
  "healthy": true,
  "circuitBreakers": {
    "serviceCircuitBreaker": {
      "name": "serviceCircuitBreaker",
      "state": "CLOSED",
      "stateDescription": " Operación normal - Todas las llamadas se procesan",
      "metrics": { ... },
      "config": { ... }
    },
    "criticalServiceCircuitBreaker": {
      "name": "criticalServiceCircuitBreaker",
      "state": "CLOSED",
      "stateDescription": " Operación normal - Todas las llamadas se procesan",
      "metrics": { ... },
      "config": { ... }
    }
  }
}
```

---

##  4. Estados del Circuit Breaker

### **CLOSED** 
- **Descripción**: Operación normal
- **Comportamiento**: Todas las llamadas se procesan normalmente
- **Transición**: → OPEN si tasa de fallos > threshold

```
CLOSED ──(failures > threshold)──→ OPEN
```

### **OPEN** 
- **Descripción**: Circuito abierto
- **Comportamiento**: Rechaza todas las llamadas inmediatamente sin procesarlas
- **Transición**: → HALF_OPEN después de `waitDurationInOpenState`

```
OPEN ──(wait time passed)──→ HALF_OPEN
```

### **HALF_OPEN** 
- **Descripción**: Verificando recuperación del servicio
- **Comportamiento**: Permite N llamadas (`permittedNumberOfCallsInHalfOpenState`)
- **Transición**: 
  - → CLOSED si las llamadas son exitosas
  - → OPEN si fallan

```
HALF_OPEN ──(success)──→ CLOSED
HALF_OPEN ──(failure)──→ OPEN
```

---

##  5. Métricas en Prometheus

Si usas **Prometheus**, accede a:

```
GET http://localhost:8080/actuator/prometheus
```

Métricas específicas del Circuit Breaker:
```
# Búsca por "resilience4j"
resilience4j_circuitbreaker_state
resilience4j_circuitbreaker_calls_total
resilience4j_circuitbreaker_calls_success_total
resilience4j_circuitbreaker_calls_failure_total
resilience4j_circuitbreaker_calls_slow_total
```

---

##  6. Monitoreo en Tiempo Real

### Opción A: Script de Monitoreo Continuo

```bash
#!/bin/bash
# monitor-circuit-breaker.sh

while true; do
    clear
    echo "=== Circuit Breaker Status ==="
    echo "Timestamp: $(date)"
    echo ""
    curl -s http://localhost:8080/api/gateway/circuit-breaker/status | jq '.[] | {name, state, metrics}'
    echo ""
    echo "Actualizando cada 5 segundos... (Ctrl+C para salir)"
    sleep 5
done
```

**Uso:**
```bash
chmod +x monitor-circuit-breaker.sh
./monitor-circuit-breaker.sh
```

### Opción B: Curl con Watch (macOS/Linux)

```bash
watch -n 2 'curl -s http://localhost:8080/api/gateway/circuit-breaker/status | jq'
```

---

##  7. Logs del Circuit Breaker

Ver logs en tiempo real:

```bash
# Solo logs del Circuit Breaker
tail -f application.log | grep "resilience4j\|CircuitBreaker"

# Con docker
docker logs -f gateway-container | grep "resilience4j"
```

---

##  8. Simular Estados del Circuit Breaker

### Para abrir el circuito (generar fallos):
```bash
# Hacer múltiples llamadas a un servicio "muerto"
for i in {1..20}; do
  curl -X GET http://localhost:8080/api/pets/list &
done

# Verificar estado
curl http://localhost:8080/api/gateway/circuit-breaker/status | jq
```

### Para cerrar el circuito (después de esperar):
```bash
# Esperar waitDurationInOpenState (20s en serviceCircuitBreaker)
sleep 25

# Hacer llamada exitosa
curl -X GET http://localhost:8080/api/pets/list

# Verificar que volvió a CLOSED
curl http://localhost:8080/api/gateway/circuit-breaker/status | jq '.serviceCircuitBreaker.state'
```

---

##  9. Comandos Útiles

```bash
# Ver todos los endpoints actuator disponibles
curl http://localhost:8080/actuator | jq

# Ver health completo
curl http://localhost:8080/actuator/health | jq

# Ver todas las métricas
curl http://localhost:8080/actuator/metrics | jq

# Ver métrica específica
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state | jq
```

---

##  Interpretación de Métricas

| Métrica | Significado |
|---------|------------|
| `successfulCalls` | Número de llamadas exitosas |
| `failedCalls` | Número de llamadas que fallaron |
| `slowCalls` | Llamadas que tardaron más que `slowCallDurationThreshold` |
| `bufferedCalls` | Total de llamadas registradas en la ventana |
| `successRate` | Porcentaje de éxito (exitosas/total) |
| `slowCallRate` | Porcentaje de llamadas lentas |

---

##  Configuración Recomendada

**Por defecto en `application.yml`:**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      serviceCircuitBreaker:
        slidingWindowSize: 15           # Últimas 15 llamadas
        failureRateThreshold: 60        # 60% de fallos = OPEN
        waitDurationInOpenState: 20s    # Esperar 20s antes de HALF_OPEN
        permittedNumberOfCallsInHalfOpenState: 5  # Permitir 5 llamadas para verificar
      
      criticalServiceCircuitBreaker:
        slidingWindowSize: 8            # Más sensible
        failureRateThreshold: 30        # 30% de fallos = OPEN (más bajo)
        waitDurationInOpenState: 45s    # Esperar más tiempo
        permittedNumberOfCallsInHalfOpenState: 2  # Menos llamadas para verificar
```

---

##  Ejemplo Completo: Flow del Circuit Breaker

```
Usuario → Llamada al servicio
           ↓
Circuit Breaker (CLOSED) → Procesa llamada normalmente
           ↓
¿Servicio responde bien?
  - Sí: contador de fallos = 0, esperar siguiente
  - No: contador de fallos ++
           ↓
¿contador de fallos > failureRateThreshold?
  - Sí → OPEN  (rechaza llamadas inmediatamente)
  - No → Continúa en CLOSED
           ↓
[Tiempo waitDurationInOpenState ha pasado]
           ↓
HALF_OPEN  (permite N llamadas de prueba)
           ↓
¿Pruebas exitosas?
  - Sí → CLOSED  (vuelve a operación normal)
  - No → OPEN  (servicio sigue caído)
```

---

Usa estos endpoints para **monitorear en tiempo real** el estado de tu gateway. 
