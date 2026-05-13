# ms-usuarios

Microservicio de gestión de usuarios para Ticketti. Expone una API REST para listar, consultar, crear, actualizar y eliminar usuarios, con documentación Swagger/OpenAPI y configuración de seguridad básica.

## Qué hace

- Permite administrar usuarios por API REST.
- Busca usuarios por id o por correo.
- Valida reglas de negocio al crear usuarios.
- Publica documentación interactiva con Swagger UI.
- Puede ejecutarse en local o con Docker Compose.

## Tecnologías

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Springdoc OpenAPI / Swagger
- MySQL
- Lombok
- Spring Cloud Config Client
- Eureka Client
- Spring Boot DevTools

## Estructura

La lógica principal vive en `src/main/java/com/ticketti/ms_usuarios/` y se organiza en:

- `controller`
- `service`
- `repository`
- `model`
- `config`
- `factory`
- `usuarios`

## Requisitos

- JDK 17
- Maven Wrapper o Maven instalado
- MySQL 8.4 si ejecutas la app fuera de Docker
- Docker y Docker Compose si quieres levantar todo en contenedores

## Configuración local

La configuración por defecto apunta a una base de datos MySQL local:

- Host: `localhost`
- Puerto: `3306`
- Base de datos: `ms_usuarios`
- Usuario: `root`
- Contraseña: vacía

En Docker Compose la base de datos se expone en `3307` y la aplicación en `8081`.

## Ejecutar en local

```powershell
mvnw.cmd spring-boot:run
```

La API queda disponible en:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Ejecutar con Docker Compose

```powershell
docker compose up --build
```

Con esta opción:

- La app responde en http://localhost:8081
- Swagger UI queda en http://localhost:8081/swagger-ui/index.html
- MySQL queda disponible en el puerto `3307`

## Autenticación y seguridad

La configuración actual usa HTTP Basic con usuarios en memoria:

- `administrador` / `Admin123` con rol `ADMIN`
- `usuario` / `usuario123` con rol `USER`

El listado de usuarios (`GET /api/v1/usuarios`) requiere rol `ADMIN`. El endpoint de Swagger está habilitado sin autenticación.

## Endpoints

Base URL: `/api/v1/usuarios`

- `GET /api/v1/usuarios`
- `GET /api/v1/usuarios/{id}`
- `GET /api/v1/usuarios?correo=correo@dominio.com`
- `POST /api/v1/usuarios`
- `PUT /api/v1/usuarios/{id}`
- `DELETE /api/v1/usuarios/{id}`

## Reglas de negocio al crear usuarios

- El usuario no puede ser nulo.
- El correo es obligatorio y debe tener formato válido.
- El correo se normaliza con `trim` y `lowercase`.
- El correo debe ser único.
- La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.
- El nombre se normaliza con `trim` antes de guardar.
- El tipo de usuario se resuelve mediante una factory según el rol recibido.

## Docker

El proyecto incluye un `Dockerfile` para compilar y ejecutar el jar, y un `docker-compose.yml` que levanta la app junto con MySQL.

## Estado del proyecto

En desarrollo. Ya se hizo la integración del DTO para la conexión con el BFF y ahora está lista para probar las validaciones del BFF.

## Siguiente paso

Probar las validaciones del BFF y ajustar el contrato de intercambio de datos si aparece algún cambio necesario.

## Autor

Ingrid Núñez Marilicán
