package com.ticketti.ms_usuarios.integration;

import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de integración del microservicio de usuarios.
// Se valida el flujo controller -> service -> repository -> base de datos H2.
@SpringBootTest(properties = {
        // Se evita depender del config server durante las pruebas.
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",

        // Se desactiva Eureka y discovery para que el test no dependa de servicios externos.
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false",

        // Base de datos en memoria solo para pruebas.
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",

        // Hibernate crea y elimina las tablas en cada ejecución de pruebas.
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // Se define un secreto solo para levantar el contexto de pruebas.
        "jwt.secret=ticketti-jwt-secret-key-2024-secure-random-256-bit-test"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Limpia la base H2 antes de cada prueba para evitar datos repetidos.
    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void crearUsuario_deberiaGuardarUsuarioEnBaseDeDatos() throws Exception {
        // Se arma el JSON manualmente porque la contraseña del modelo es WRITE_ONLY.
        String usuarioJson = """
                {
                    "nombre": "Usuario Prueba",
                    "correo": "test@ticketti.cl",
                    "contrasena": "Password123",
                    "rol": "cliente",
                    "telefono": "912345678",
                    "direccion": "Direccion de prueba 123",
                    "aceptaTerminos": true,
                    "aceptaPrivacidad": true
                }
                """;

        // Se llama al endpoint real de creación de usuario.
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre").value("Usuario Prueba"))
                .andExpect(jsonPath("$.correo").value("test@ticketti.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void validarCredenciales_conUsuarioRegistrado_deberiaRetornarCredencialesValidas() throws Exception {
        // Primero se registra un usuario válido.
        String usuarioJson = """
                {
                    "nombre": "Usuario Login",
                    "correo": "login@ticketti.cl",
                    "contrasena": "Password123",
                    "rol": "cliente",
                    "telefono": "912345679",
                    "direccion": "Direccion login 123",
                    "aceptaTerminos": true,
                    "aceptaPrivacidad": true
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson))
                .andExpect(status().isCreated());

        // Luego se validan las credenciales del usuario recién creado.
        String loginJson = """
                {
                    "correo": "login@ticketti.cl",
                    "contrasena": "Password123"
                }
                """;

        // Si el flujo está integrado correctamente, debe responder credenciales válidas.
        mockMvc.perform(post("/api/v1/usuarios/validar-credenciales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.correo").value("login@ticketti.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.mensaje").value("Credenciales válidas"));
    }

    @Test
    void validarCredenciales_conContrasenaIncorrecta_deberiaRetornarUnauthorized() throws Exception {
        // Se registra un usuario válido.
        String usuarioJson = """
                {
                    "nombre": "Usuario Error",
                    "correo": "error@ticketti.cl",
                    "contrasena": "Password123",
                    "rol": "cliente",
                    "telefono": "912345680",
                    "direccion": "Direccion error 123",
                    "aceptaTerminos": true,
                    "aceptaPrivacidad": true
                }
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson))
                .andExpect(status().isCreated());

        // Se intenta validar con una contraseña incorrecta.
        String loginJson = """
                {
                    "correo": "error@ticketti.cl",
                    "contrasena": "PasswordIncorrecta123"
                }
                """;

        // El microservicio debe rechazar las credenciales.
        mockMvc.perform(post("/api/v1/usuarios/validar-credenciales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }
}