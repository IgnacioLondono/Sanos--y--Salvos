# 3 Snippets listos para defensa (Parcial 2)

## 1) API Gateway (patron Gateway)

```java
// gateway/src/main/java/com/sanos/gateway/GatewayApplication.java
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**Como defenderlo (30s):**  
"Este componente implementa el patron API Gateway. Centraliza la entrada al sistema, aplica seguridad y enruta a microservicios. Asi el frontend no depende de URLs internas y desacoplamos clientes del backend."

---

## 2) BFF (patron Backend For Frontend)

```java
// bff/src/main/java/com/sanos/bff/controller/BffController.java
@RestController
@RequestMapping("/api/bff")
public class BffController {
    // El BFF agrega informacion de varios servicios para vistas de dashboard
}
```

**Como defenderlo (30s):**  
"Aqui aplicamos BFF para adaptar backend a necesidades del frontend. En vez de multiples llamadas desde navegador, el BFF agrega y simplifica respuestas. Esto mejora latencia percibida y encapsula logica de integracion."

---

## 3) Event-Driven con RabbitMQ (publicador/suscriptor)

```java
// services/reports-service/src/main/java/com/sanos/reportsservice/messaging/ReportEventPublisher.java
// Publica evento de negocio report.created cuando se crea un reporte
```

```java
// services/audit-service/src/main/java/com/sanos/auditservice/messaging/ReportCreatedEventListener.java
// Consume report.created para registrar trazabilidad en auditoria
```

**Como defenderlo (30s):**  
"Este flujo aplica arquitectura orientada a eventos. Reports publica `report.created` y audit/matching consumen. Con eso desacoplamos procesos secundarios, mejoramos resiliencia y evitamos bloquear la transaccion principal."

---

## Frase de cierre sugerida (15s)

"Con estos tres patrones (Gateway, BFF y Event-Driven) resolvemos desacoplamiento, escalabilidad y mantenibilidad en frontend y backend, alineado con la arquitectura de microservicios exigida por la rubrica."
