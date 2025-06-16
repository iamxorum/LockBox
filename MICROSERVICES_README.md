# LockBox - Arhitectură Micro-servicii

## Prezentare Generală

Această implementare transformă aplicația LockBox într-o arhitectură de micro-servicii completă, respectând toate cerințele specificate:

## 🏗️ Componente Arhitecturale

### 1. Configurarea Unitară a Micro-serviciilor

- **Profile-based Configuration** - Configurare centralizată prin profile Spring
- **Eureka Server** - Service Discovery și Registry
- **API Gateway** - Gateway pentru rutare și load balancing

### 2. Service Discovery Funcțional

- **Eureka Client** integrat în serviciul principal
- **Auto-registration** și **health checks**
- **Service discovery** pentru comunicarea inter-servicii

### 3. Scalabilitate și Load-Balancing

- **Spring Cloud LoadBalancer** pentru distribuirea traficului
- **Multiple instances** suportate prin Docker Compose
- **Circuit Breaker** pentru resilience

### 4. Monitorizare, Metrici și Logging

- **Prometheus** - Colectarea metricilor
- **Grafana** - Vizualizarea metricilor
- **Zipkin** - Distributed tracing
- **ELK Stack** (Elasticsearch, Logstash, Kibana) - Log aggregation
- **Spring Boot Actuator** - Health checks și metrici

### 5. Elemente de Securitate

- **Spring Security** configurat pentru micro-servicii
- **JWT tokens** pentru autentificare inter-servicii
- **HTTPS** support
- **Service-to-service authentication**

### 6. Asigurare Reziliență

- **Circuit Breaker** (Resilience4j)
- **Retry mechanisms**
- **Rate limiting**
- **Fallback methods**
- **Health checks** și **graceful degradation**

### 7. Design Patterns Specifice

- **API Gateway Pattern**
- **Service Registry Pattern**
- **Circuit Breaker Pattern**
- **Bulkhead Pattern**
- **Saga Pattern** (pentru tranzacții distribuite)

## 🚀 Pornirea Aplicației

### Prerequisite

- Docker și Docker Compose
- Java 21
- Maven 3.8+

### Pornire Rapidă

```bash
# Clonează repository-ul
git clone <repository-url>
cd lockbox

# Construiește aplicația
./mvnw clean package -DskipTests

# Pornește întreaga infrastructură
docker-compose up -d

# Verifică statusul serviciilor
docker-compose ps
```

### Servicii Disponibile

| Serviciu | Port | Descriere |
|----------|------|-----------|
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8080 | Gateway Principal |
| LockBox Service | 8081 | Serviciul Principal |
| Prometheus | 9090 | Metrici |
| Grafana | 3000 | Dashboard Monitorizare |
| Zipkin | 9411 | Distributed Tracing |
| Kibana | 5601 | Log Visualization |
| RabbitMQ | 15672 | Message Queue Management |
| PostgreSQL | 5432 | Database Principal |
| Redis | 6379 | Cache și Session Store |

## 📊 Monitorizare și Observabilitate

### Metrici (Prometheus + Grafana)

- Accesează Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Metrici custom pentru LockBox disponibile

### Distributed Tracing (Zipkin)

- Accesează Zipkin: http://localhost:9411
- Trace-uri complete pentru request-uri inter-servicii
- Correlation IDs pentru tracking

### Logging (ELK Stack)

- Kibana: http://localhost:5601
- Loguri centralizate cu correlation IDs
- Structured logging cu metadata

## 🔧 Configurare și Customizare

### Profiles Disponibile

- `dev` - Dezvoltare locală
- `docker` - Containerizat
- `prod` - Producție

### Configurări Importante

```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.default.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.default.wait-duration-in-open-state=10s

# Rate Limiting
resilience4j.ratelimiter.instances.default.limit-for-period=10
resilience4j.ratelimiter.instances.default.limit-refresh-period=1s

# Retry
resilience4j.retry.instances.default.max-attempts=3
resilience4j.retry.instances.default.wait-duration=1s
```

## 🧪 Testarea Funcționalităților

### Endpoints de Test

```bash
# Health Check
curl http://localhost:8081/actuator/health

# Service Discovery
curl http://localhost:8081/api/microservices/services

# Circuit Breaker Test
curl http://localhost:8081/api/microservices/users/1

# Metrici
curl http://localhost:8081/actuator/prometheus
```

### Simularea Erorilor

```bash
# Oprește un serviciu pentru a testa circuit breaker
docker-compose stop user-service

# Testează fallback-ul
curl http://localhost:8081/api/microservices/users/1
```

## 📈 Scalare

### Scalarea Serviciilor

```bash
# Scalează serviciul principal
docker-compose up -d --scale lockbox-service=3

# Verifică load balancing
curl http://localhost:8080/api/microservices/health
```

## 🔒 Securitate

### Configurări de Securitate

- Autentificare bazată pe token
- HTTPS pentru comunicarea inter-servicii
- Rate limiting pentru protecție DDoS
- Health checks securizate

## 📝 Logging Distribuit

### Correlation IDs

Fiecare request primește un correlation ID unic pentru tracking:

```json
{
  "correlationId": "123e4567-e89b-12d3-a456-426614174000",
  "service": "lockbox-service",
  "operation": "get-user",
  "userId": "user123",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## 🚨 Alerting și Notificări

### Configurări Grafana

- Dashboard-uri pre-configurate
- Alerte pentru:
  - Circuit breaker activation
  - High error rates
  - Service unavailability
  - Resource utilization

## 📚 Documentație API

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI Spec: http://localhost:8081/v3/api-docs

## 🔄 CI/CD Integration

Aplicația este pregătită pentru:

- **Docker builds**
- **Kubernetes deployment**
- **Health checks** pentru orchestration
- **Graceful shutdown**

## 🛠️ Troubleshooting

### Probleme Comune

1. **Serviciile nu se înregistrează în Eureka**
   - Verifică configurația `eureka.client.service-url.defaultZone`
   - Asigură-te că Eureka Server rulează

2. **Circuit Breaker nu funcționează**
   - Verifică configurația Resilience4j
   - Testează cu multiple request-uri eșuate

3. **Metrici lipsesc din Prometheus**
   - Verifică endpoint-ul `/actuator/prometheus`
   - Configurează corect `management.endpoints.web.exposure.include`

### Logs Utile

```bash
# Logs pentru serviciul principal
docker-compose logs -f lockbox-service

# Logs pentru Eureka
docker-compose logs -f eureka-server

# Logs pentru toate serviciile
docker-compose logs -f
```

## 🎯 Beneficii Implementate

✅ **Configurare unitară** - Profile-based configuration  
✅ **Service Discovery** - Eureka Server funcțional  
✅ **Scalabilitate** - Load balancing și multiple instances  
✅ **Monitorizare** - Prometheus, Grafana, Zipkin, ELK  
✅ **Securitate** - Spring Security pentru micro-servicii  
✅ **Reziliență** - Circuit breaker, retry, rate limiting  
✅ **Design Patterns** - Gateway, Registry, Circuit Breaker  

Această implementare oferă o arhitectură de micro-servicii completă și robustă pentru aplicația LockBox, respectând toate cerințele specificate în proiect. 