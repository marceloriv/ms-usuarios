package com.ticketti.ms_usuarios.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketti.ms_usuarios.dto.LoginResponse;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesRequest;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesResponse;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

//@RequestMapping, @GetMapping @PostMapping  definen la ruta del controlador
//@RequestBody para recibir el cuerpo de la solicitud en formato Json y spring lo mapea y pasa al service
//@ApiResponse y @ApiResponses para documentar la API con Swagger, indicando los posibles codigos de respuesta y lo que significa
//ResponseEntity para manejar las respuestas HTTP, permitiendo devolver el codigo de estado y el cuerpo de la respuesta de manera flexible

@RestController
@Slf4j
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
	// private final para inyectar el servicio de usuario y manejar la logica de
	// negocio en el controlador
	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	// listado de usuarios sin restriccion de rol en la logica del servicio
	@Operation(summary = "Listar todos los usuarios - TEST DOCKER REBUILD", description = "Obtiene todos los usuarios del sistema Ticketti. Cambio prueba Docker.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioModel.class)))),
			@ApiResponse(responseCode = "204", description = "No hay usuarios registrados")
	})
	@GetMapping
	public ResponseEntity<?> listarUsuarios() {
		List<UsuarioModel> usuarios = usuarioService.listar();
		if (usuarios.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(usuarios);
	}

	// obtener usuario por id, para admin y usuario mismo
	@Operation
	@GetMapping("/{id}")
	public ResponseEntity<UsuarioModel> obtenerUsuarioPorId(@PathVariable Long id) {
		return usuarioService.obtenerPorId(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// obtener usuario por correo
	@Operation(summary = "Buscar usuario por correo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Usuario encontrado"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	@GetMapping(params = "correo")
	public ResponseEntity<UsuarioModel> buscarUsuarioPorCorreo(@RequestParam String correo) {
		return usuarioService.obtenerPorCorreo(correo)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// crear usuario, para admin y registro publico
	@Operation(summary = "Crear usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
			@ApiResponse(responseCode = "409", description = "El usuario ya existe en la base de datos"),
			@ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
	})

	@PostMapping
	public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioModel usuario) {
		try {
			UsuarioModel usuarioCreado = usuarioService.crearUsuario(usuario);
			return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
		} catch (IllegalArgumentException e) {
			if ("El correo ya existe".equals(e.getMessage())) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(Map.of("mensaje", e.getMessage()));
			}
			return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
		}
	}

	// actualizar los usuarios del sistema uwu
	@Operation(summary = "Actualizar usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
	})
	@PutMapping("/{id}")
	public ResponseEntity<UsuarioModel> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioModel usuario) {
		return usuarioService.actualizar(id, usuario)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// solo el usuario adminPlataforma puede cambiar el rol del los usruarios
	@PatchMapping("/{id}/rol")
	public ResponseEntity<?> cambiarRol(
			@PathVariable Long id,
			@RequestBody Map<String, String> body) {
		try {
			String nuevoRol = body.get("rol");
			UsuarioModel usuarioActualizado = usuarioService.cambiarRol(id, nuevoRol);
			return ResponseEntity.ok(usuarioActualizado);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
		}
	}

	// eliminar usuario y el administrador puede eliminar a cualquier usuario, el
	// usuario puede eliminar su propia cuenta
	@Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema. ADMIN y ADMINPLATAFORMA pueden eliminar cualquier cuenta. Un usuario autenticado solo puede eliminar su propia cuenta.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente"),
			@ApiResponse(responseCode = "401", description = "Usuario no autenticado o datos de autenticación no enviados"),
			@ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta cuenta"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> eliminarUsuario(
			@PathVariable Long id,
			@RequestHeader(value = "X-Usuario-Id", required = false) Long idUsuarioAutenticado,
			@RequestHeader(value = "X-Usuario-Rol", required = false) String rolUsuarioAutenticado) {
		if (idUsuarioAutenticado == null || rolUsuarioAutenticado == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("mensaje", "No se pudo identificar al usuario autenticado"));
		}

		try {
			boolean eliminado = usuarioService.eliminarSiAutorizado(
					id,
					idUsuarioAutenticado,
					rolUsuarioAutenticado);

			if (!eliminado) {
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));

		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("mensaje", e.getMessage()));
		}
	}
	// Validar credenciales y generar JWT para autenticación, para login y validación de credenciales
	@Operation(summary = "Validar credenciales", description = "Valida correo y contraseña para autenticación")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Credenciales válidas"),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas")
	})
	@PostMapping("/validar-credenciales")
	public ResponseEntity<ValidarCredencialesResponse> validarCredenciales(
			@RequestBody ValidarCredencialesRequest credenciales) {
		ValidarCredencialesResponse response = usuarioService.validarCredenciales(credenciales);
		if (response.valido()) {
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
	// Login y generación de JWT para autenticación, para login y validación de credenciales
	@Operation(summary = "Login y generación de JWT", description = "Valida credenciales y retorna un token JWT")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login exitoso, token generado"),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas")
	})
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody ValidarCredencialesRequest credenciales) {
		try {
			LoginResponse response = usuarioService.login(credenciales);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("mensaje", e.getMessage()));
		}
	}

}
