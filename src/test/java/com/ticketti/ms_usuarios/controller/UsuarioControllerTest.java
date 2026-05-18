package com.ticketti.ms_usuarios.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesRequest;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesResponse;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

	@Mock
	private UsuarioService usuarioService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
				.setControllerAdvice(new UsuarioControllerAdvice())
				.setValidator(validator)
				.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
				.build();
	}

	@Test
	void listarUsuariosDevuelveNoContentCuandoNoHayRegistros() throws Exception {
		when(usuarioService.listar()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/usuarios"))
				.andExpect(status().isNoContent());
	}

	@Test
	void listarUsuariosDevuelveOkConLaLista() throws Exception {
		UsuarioModel usuario = crearUsuario(1L, "Ana", "ana@example.com", "ADMIN");
		when(usuarioService.listar()).thenReturn(List.of(usuario));

		mockMvc.perform(get("/api/v1/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].correo").value("ana@example.com"));
	}

	@Test
	void obtenerUsuarioPorIdDevuelveOkCuandoExiste() throws Exception {
		when(usuarioService.obtenerPorId(1L)).thenReturn(java.util.Optional.of(crearUsuario(1L, "Ana", "ana@example.com", "ADMIN")));

		mockMvc.perform(get("/api/v1/usuarios/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana"));
	}

	@Test
	void obtenerUsuarioPorIdDevuelveNotFoundCuandoNoExiste() throws Exception {
		when(usuarioService.obtenerPorId(1L)).thenReturn(java.util.Optional.empty());

		mockMvc.perform(get("/api/v1/usuarios/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void buscarUsuarioPorCorreoDevuelveOkCuandoExiste() throws Exception {
		when(usuarioService.obtenerPorCorreo("ana@example.com"))
				.thenReturn(java.util.Optional.of(crearUsuario(1L, "Ana", "ana@example.com", "ADMIN")));

		mockMvc.perform(get("/api/v1/usuarios").param("correo", "ana@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correo").value("ana@example.com"));
	}

	@Test
	void buscarUsuarioPorCorreoDevuelveNotFoundCuandoNoExiste() throws Exception {
		when(usuarioService.obtenerPorCorreo(anyString())).thenReturn(java.util.Optional.empty());

		mockMvc.perform(get("/api/v1/usuarios").param("correo", "ana@example.com"))
				.andExpect(status().isNotFound());
	}

	@Test
	void crearUsuarioDevuelveCreatedCuandoEsValido() throws Exception {
		when(usuarioService.crearUsuario(any(UsuarioModel.class))).thenReturn(crearUsuario(1L, "Ana", "ana@example.com", "ADMIN"));

		mockMvc.perform(post("/api/v1/usuarios")
					.contentType(APPLICATION_JSON)
					.content("{\"nombre\":\"Ana\",\"correo\":\"ana@example.com\",\"rol\":\"ADMIN\",\"telefono\":\"123456789\",\"direccion\":\"Calle Principal 123\",\"contrasena\":\"Secret123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void crearUsuarioDevuelveConflictCuandoElCorreoYaExiste() throws Exception {
		when(usuarioService.crearUsuario(any(UsuarioModel.class))).thenThrow(new IllegalArgumentException("El correo ya existe"));

		mockMvc.perform(post("/api/v1/usuarios")
					.contentType(APPLICATION_JSON)
					.content("{\"nombre\":\"Ana\",\"correo\":\"ana@example.com\",\"rol\":\"ADMIN\",\"telefono\":\"123456789\",\"direccion\":\"Calle Principal 123\",\"contrasena\":\"Secret123\"}"))
				.andExpect(status().isConflict())
				.andExpect(content().string(containsString("El correo ya existe")));
	}



	@Test
	void actualizarUsuarioDevuelveNotFoundCuandoNoExiste() throws Exception {
		UsuarioModel usuario = crearUsuario(null, "Ana Maria", "ana.maria@example.com", "ADMIN");
		usuario.setContrasena("Secret123");
		when(usuarioService.actualizar(anyLong(), any(UsuarioModel.class))).thenReturn(java.util.Optional.empty());

		mockMvc.perform(put("/api/v1/usuarios/1")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(usuario)))
				.andExpect(status().isNotFound());
	}

	@Test
	void eliminarUsuarioDevuelveOkCuandoSeElimina() throws Exception {
		when(usuarioService.eliminar(1L)).thenReturn(true);

		mockMvc.perform(delete("/api/v1/usuarios/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensaje").value("Usuario eliminado correctamente"));
	}

	@Test
	void eliminarUsuarioDevuelveNotFoundCuandoNoExiste() throws Exception {
		when(usuarioService.eliminar(1L)).thenReturn(false);

		mockMvc.perform(delete("/api/v1/usuarios/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void validarCredencialesDevuelveOkCuandoSonCorrectas() throws Exception {
		when(usuarioService.validarCredenciales(any(ValidarCredencialesRequest.class)))
				.thenReturn(new ValidarCredencialesResponse(true, 1L, "ana@example.com", "Ana", "ADMIN", "Credenciales válidas"));

		mockMvc.perform(post("/api/v1/usuarios/validar-credenciales")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(new ValidarCredencialesRequest("ana@example.com", "Secret123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valido").value(true));
	}

	@Test
	void validarCredencialesDevuelveUnauthorizedCuandoSonInvalidas() throws Exception {
		when(usuarioService.validarCredenciales(any(ValidarCredencialesRequest.class)))
				.thenReturn(new ValidarCredencialesResponse(false, null, null, null, null, "Credenciales inválidas"));

		mockMvc.perform(post("/api/v1/usuarios/validar-credenciales")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(new ValidarCredencialesRequest("ana@example.com", "wrong"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.valido").value(false));
	}

	private UsuarioModel crearUsuario(Long id, String nombre, String correo, String rol) {
		UsuarioModel usuario = new UsuarioModel();
		usuario.setId(id);
		usuario.setNombre(nombre);
		usuario.setCorreo(correo);
		usuario.setRol(rol);
		usuario.setTelefono("123456789");
		usuario.setDireccion("Calle Principal 123");
		return usuario;
	}
}