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
