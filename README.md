# ms-usuarios

Microservicio de gestion de usuarios con Spring Boot, JPA, MySQL y Spring Security.

## Cambios implementados en esta fase

- Validacion de negocio en creacion de usuario desde service.
- Validacion de correo con regex flexible (acepta .cl, .com, .org, etc.).
- Validacion de contrasena fuerte (minimo 8, mayuscula, minuscula y numero).
- Validacion de telefono con formato numerico de 9 digitos.
- Normalizacion de correo (trim y lowercase) antes de guardar.
- Hash de contrasena con BCrypt antes de persistir.
- Respuesta 409 para correo duplicado y 400 para errores de validacion en el endpoint de creacion.
- Configuracion de seguridad con usuarios en memoria para pruebas:
- Separacion de configuracion OpenAPI en clase dedicada.
- Dockerfile base para build y ejecucion del jar.

## Endpoints principales

Base URL: http://localhost:8080/api/v1/usuarios

- GET /api/v1/usuarios
- GET /api/v1/usuarios/{id}
- GET /api/v1/usuarios?correo=correo@dominio.com
- POST /api/v1/usuarios
- PUT /api/v1/usuarios/{id}
- DELETE /api/v1/usuarios/{id}

## Reglas actuales de validacion en POST /usuarios

- nombre obligatorio
- correo obligatorio y con formato valido
- correo unico
- contrasena obligatoria y fuerte
- telefono obligatorio y con 9 digitos numericos

## Notas de prueba

- Si el correo ya existe, la API responde 409.
- Si hay datos invalidos, la API responde 400.
- En GET la contrasena no se expone en JSON.

## Ejecucion local

Compilar:

mvnw.cmd clean package

Levantar app:

mvnw.cmd spring-boot:run

## Siguiente paso 

Implementar autenticacion con JWT y mover el manejo de excepciones a un RestControllerAdvice global.
# MS Usuarios

Microservicio encargado de la gestión de usuarios del Software Ticketti.

## Funcionalidades iniciales
- Estructura base del microservicio
- Organización por capas:
  - controller
  - service
  - repository
  - model
  - config
- Configuración inicial en Spring Boot

## Tecnologías utilizadas

Estas son las herramientas principales que usa el microservicio y el rol que cumplen:

- Java 17: lenguaje base del proyecto.
- Spring Boot 4.0.5: framework principal para construir la API REST.
- Spring Web MVC: manejo de controladores, rutas y respuestas HTTP.
- Spring Data JPA: acceso a la base de datos con repositorios.
- Spring Security: control de acceso, autenticación y protección de endpoints.
- Springdoc OpenAPI / Swagger: documentación automática de la API.
- MySQL: base de datos relacional donde se guardan los usuarios.
- Lombok: reduce código repetitivo como getters, setters y constructores.
- Spring Cloud Config Client: permite consumir configuración externa si se usa servidor de configuración.
- Eureka Client: registra el microservicio en Eureka para descubrimiento de servicios.
- Spring Boot DevTools: facilita el desarrollo con recarga automática.
- JWT: tokens para autenticación sin estado en futuras etapas.
  

## Estructura del proyecto
src/main/java/com/ticketti/ms_usuarios/
- controller
- service
- repository
- model
- config

## Estado actual
En desarrollo.  
Actualmente se está construyendo la base del microservicio de usuarios.

## Autor
Ingrid Núñez Marilicán 
