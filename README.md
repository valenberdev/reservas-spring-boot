# Reservas API

![CI](https://github.com/valenberdev/reservas-spring-boot/actions/workflows/ci.yml/badge.svg)

API REST para reservar recursos (canchas, salas, turnos) por franjas horarias. El foco del proyecto es **garantizar que nunca existan dos reservas activas superpuestas sobre el mismo recurso**, aunque lleguen decenas de solicitudes simultáneas, y demostrarlo con tests.

## Stack

- Java 21, Spring Boot 4, Spring Data JPA
- PostgreSQL 16 con migraciones Flyway
- Spring Security como resource server: autenticación JWT (HS256) y roles
- Tests: JUnit 5, Mockito, MockMvc y Testcontainers (PostgreSQL real)
- Documentación OpenAPI con Swagger UI
- Docker (multi-stage) y GitHub Actions

## Cómo ejecutarlo

Requisitos: JDK 21 y Docker.

```bash
# 1. Levantar PostgreSQL (puerto 5440)
docker compose up -d

# 2. Correr la app (perfil dev por defecto)
./mvnw spring-boot:run
```

La documentación interactiva queda en <http://localhost:8080/swagger-ui/index.html>.

Para crear un usuario administrador al arrancar, definí estas variables de entorno (las credenciales nunca van en el repositorio):

```bash
RESERVAS_ADMIN_EMAIL=admin@ejemplo.com RESERVAS_ADMIN_PASSWORD=una-clave-segura ./mvnw spring-boot:run
```

### Con Docker

```bash
cp .env.example .env        # completá JWT_SECRET con un valor aleatorio de 32+ caracteres
docker compose --profile app up --build
```

El contenedor corre con el perfil `prod`: si falta una credencial, la aplicación **se niega a arrancar**.

### Tests

```bash
./mvnw test
```

Necesita Docker abierto: los tests de integración levantan su propio PostgreSQL con Testcontainers, sin depender de la base de desarrollo.
