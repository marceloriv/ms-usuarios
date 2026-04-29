# ms-usuarios

Microservicio de gestión de usuarios para el Software Ticketti. Está construido con Spring Boot, JPA, MySQL, Spring Security y Springdoc OpenAPI para documentar la API.

## Resumen del proyecto

Este servicio centraliza el alta, consulta, edición y eliminación de usuarios. También aplica validaciones de negocio, cifrado de contraseñas y documentación automática con Swagger.

## Tecnologías utilizadas

- Java 17: lenguaje base del proyecto.
- Spring Boot 4.0.6: framework principal para construir la API REST.
- Spring Web MVC: manejo de controladores, rutas y respuestas HTTP.
- Spring Data JPA: acceso a la base de datos con repositorios.
- Spring Security: control de acceso, autenticación y protección de endpoints.
- Springdoc OpenAPI / Swagger: documentación automática de la API.
- MySQL: base de datos relacional donde se guardan los usuarios.
- Lombok: reduce código repetitivo como getters, setters y constructores.
- Spring Cloud Config Client: permite consumir configuración externa si se usa servidor de configuración.
- Eureka Client: registra el microservicio en Eureka para descubrimiento de servicios.
- Spring Boot DevTools: facilita el desarrollo con recarga automática.

## Estructura del proyecto

`src/main/java/com/ticketti/ms_usuarios/`

- `controller`
- `service`
- `repository`
- `model`
- `config`

## Configuración de Swagger

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints principales

Base URL: http://localhost:8080/api/v1/usuarios

- GET /api/v1/usuarios
- GET /api/v1/usuarios/{id}
- GET /api/v1/usuarios?correo=correo@dominio.com
- POST /api/v1/usuarios
- PUT /api/v1/usuarios/{id}
- DELETE /api/v1/usuarios/{id}

## Reglas actuales de validación en POST /usuarios

- nombre obligatorio
- correo obligatorio y con formato válido
- correo único
- contraseña obligatoria y fuerte
- teléfono obligatorio y con 9 dígitos numéricos

## Cambios implementados

- Validación de negocio en creación de usuario desde service.
- Validación de correo con regex flexible para dominios como `.cl`, `.com` y `.org`.
- Validación de contraseña fuerte con mínimo 8 caracteres, mayúscula, minúscula y número.
- Validación de teléfono con formato numérico de 9 dígitos.
- Normalización de correo con `trim` y `lowercase` antes de guardar.
- Hash de contraseña con BCrypt antes de persistir.
- Respuesta 409 para correo duplicado y 400 para errores de validación.
- Configuración de seguridad con usuarios en memoria para pruebas.
- Separación de la configuración OpenAPI en una clase dedicada.
- Dockerfile base para build y ejecución del jar.
- Workflow de GitHub Actions para publicar la imagen Docker en Docker Hub.

## Docker y despliegue

### Build local

```powershell
mvnw.cmd clean package
docker compose up --build
```

### Ejecución local sin Docker

```powershell
mvnw.cmd spring-boot:run
```

## GitHub Actions

El archivo `.github/workflows/docker-publish.yml` publica la imagen Docker cuando hay un push en la rama `feature/base-ms-usuarios`.



## Estado actual

En desarrollo. Actualmente se está construyendo la base del microservicio de usuarios.

## Siguiente paso

Implementar autenticación con JWT y mover el manejo de excepciones a un `RestControllerAdvice` global.

## Autor

Ingrid Núñez Marilicán
