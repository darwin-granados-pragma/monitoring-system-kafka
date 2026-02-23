# Monitoring System - Kafka + DLT (Educativo)

## Descripcion
Proyecto educativo en Java (Spring Boot WebFlux + Clean Architecture) para:
1. Recibir logs por HTTP (`topic`, `description`).
2. Publicarlos en Kafka (`topic` principal).
3. Consumir y validar mensajes.
4. Redirigir mensajes malformados a un Dead Letter Topic (DLT).

## Arquitectura de flujo
```text
Router Function (POST /api/v1/logs/router)
    ↓
UseCase (RouteLogUseCase)
    ↓
Kafka Producer Adapter → monitoring.logs.main
    ↓
Kafka Consumer ← monitoring.logs.main
    ↓
EventHandler (Retry básico reactivo)
    ↓
UseCase (ProcessLogUseCase)
    ├── válido: log de procesamiento exitoso
    └── inválido: Kafka Producer Adapter → monitoring.logs.dlt
```

## Prerequisitos
- Java 21
- Gradle Wrapper (incluido en el repo)
- Docker y Docker Compose
- Kafka disponible en `localhost:9092`
- Topics creados:
  - `monitoring.logs.main`
  - `monitoring.logs.dlt`

## Levantar Kafka local
`docker-compose.yml` de referencia:

Comando dentro de la carpeta deployment para levantar Kafka localmente:
```bash
docker compose up -d
```

Crear topics:
```bash
docker exec -it kafka-local kafka-topics --create --topic monitoring.logs.main --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker exec -it kafka-local kafka-topics --create --topic monitoring.logs.dlt --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker exec -it kafka-local kafka-topics --list --bootstrap-server localhost:9092
```

## Variables de entorno
```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC_MAIN=monitoring.logs.main
export KAFKA_TOPIC_DLT=monitoring.logs.dlt
export KAFKA_CONSUMER_GROUP_ID=monitoring-system-group
export KAFKA_CONSUMER_OFFSET_RESET=earliest
export KAFKA_PRODUCER_ACKS=all
export KAFKA_PRODUCER_RETRIES=3
export KAFKA_LISTENER_CONCURRENCY=1
```

## Comando de ejecucion
Desde la raiz del proyecto:
```bash
./gradlew bootRun
```

## Endpoint y ejemplo curl
Endpoint:
- `POST http://localhost:8080/api/v1/logs/router`

`curl`:
```bash
curl --location 'http://localhost:8080/api/v1/logs/router' \
--header 'Content-Type: application/json' \
--header 'traceId: 7f6d826e-2754-4bd7-a097-0b3c4f2df8e0' \
--data '{
  "topic": "payments-monitoring",
  "description": "Timeout en procesamiento de transaccion 91273"
}'
```

## Respuesta esperada (200)
```json
{
  "data": {
    "messageId": "f2ca76ab-8254-4d5c-87ff-3d4c0d369fd4",
    "status": "PUBLISHED",
    "targetTopic": "monitoring.logs.main",
    "receivedTopic": "payments-monitoring"
  },
  "success": true,
  "message": "Evento publicado correctamente"
}
```

# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**