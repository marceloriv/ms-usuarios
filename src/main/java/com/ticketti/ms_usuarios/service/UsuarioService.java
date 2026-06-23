package com.ticketti.ms_usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ticketti.ms_usuarios.dto.LoginResponse;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesRequest;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesResponse;
import com.ticketti.ms_usuarios.factory.UsuarioFactory;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import com.ticketti.ms_usuarios.security.JwtService;
import com.ticketti.ms_usuarios.usuarios.Usuario;

@Service
public class UsuarioService {

	// Patrones para validar formato de correo y contraseña segura
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

	// Constantes para manejar intentos fallidos y bloqueo
	private static final int MAX_INTENTOS_FALLIDOS = 3;
	private static final int DURACION_BLOQUEO_MINUTOS = 15;

	// Constantes para registrar la versión aceptada de documentos legales
	private static final String VERSION_TERMINOS_ACTUAL = "1.0";
	private static final String VERSION_PRIVACIDAD_ACTUAL = "1.0";

	// Servicio que contiene la lógica de negocio
	private final UsuarioRepository usuarioRepository;
	private final UsuarioFactory usuarioFactory;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public UsuarioService(UsuarioRepository usuarioRepository,
			UsuarioFactory usuarioFactory,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioFactory = usuarioFactory;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public List<UsuarioModel> listar() {
		return usuarioRepository.findAll();
	}

	public Optional<UsuarioModel> obtenerPorId(Long id) {
		return usuarioRepository.findById(id);
	}

	public Optional<UsuarioModel> obtenerPorCorreo(String correo) {
		if (correo == null) {
			return Optional.empty();
		}
		return usuarioRepository.findByCorreoIgnoreCase(correo.trim());
	}



	// Actualizar los usuarios del sistema
	public Optional<UsuarioModel> actualizar(Long id, UsuarioModel usuarioActualizado) {
		return usuarioRepository.findById(id)
				.map(usuarioExistente -> {
					usuarioExistente.setNombre(usuarioActualizado.getNombre().trim());

					String correoNormalizado = usuarioActualizado.getCorreo().trim().toLowerCase();
					usuarioExistente.setCorreo(correoNormalizado);

					if (usuarioActualizado.getContrasena() != null && !usuarioActualizado.getContrasena().isBlank()) {
						usuarioExistente.setContrasena(passwordEncoder.encode(usuarioActualizado.getContrasena()));
					}

					usuarioExistente.setRol(usuarioActualizado.getRol());
					usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
					usuarioExistente.setDireccion(usuarioActualizado.getDireccion());

					return usuarioRepository.save(usuarioExistente);
				});
	}

	// Crear usuarios con validaciones de correo único, formato correcto y
	// contraseña segura
	public UsuarioModel crearUsuario(UsuarioModel nuevoUsuario) {
		if (nuevoUsuario == null) {
			throw new IllegalArgumentException("El usuario es obligatorio");
		}

		// Validación de correo
		if (nuevoUsuario.getCorreo() == null || !EMAIL_PATTERN.matcher(nuevoUsuario.getCorreo()).matches()) {
			throw new IllegalArgumentException("El correo no tiene un formato valido");
		}

		String correoNormalizado = nuevoUsuario.getCorreo().trim().toLowerCase();

		// Validación de correo único
		if (usuarioRepository.findByCorreoIgnoreCase(correoNormalizado).isPresent()) {
			throw new IllegalArgumentException("El correo ya existe");
		}

		// Validación de contraseña segura
		if (nuevoUsuario.getContrasena() == null
				|| !PASSWORD_PATTERN.matcher(nuevoUsuario.getContrasena()).matches()) {
			throw new IllegalArgumentException(
					"La contrasena debe tener minimo 8 caracteres, mayuscula, minuscula y numero");
		}

		// Validación de aceptación de términos y condiciones y política de privacidad
		if (!nuevoUsuario.isAceptaTerminos()) {
			throw new IllegalArgumentException("Debe aceptar los Términos y Condiciones para registrarse");
		}

		if (!nuevoUsuario.isAceptaPrivacidad()) {
			throw new IllegalArgumentException("Debe aceptar la Política de Privacidad para registrarse");
		}

		// Se utiliza el factory para crear el usuario con el rol correspondiente
		Usuario tipoUsuario = usuarioFactory.obtenerUsuario(nuevoUsuario.getRol());
		tipoUsuario.crearUsuario(nuevoUsuario);

		// Se normaliza el correo a minúsculas
		nuevoUsuario.setId(null);
		nuevoUsuario.setNombre(nuevoUsuario.getNombre().trim());
		nuevoUsuario.setCorreo(correoNormalizado);
		nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));

		// Se registran las fechas y versiones actuales de los documentos legales
		LocalDateTime fechaAceptacion = LocalDateTime.now();
		nuevoUsuario.setFechaAceptacionTerminos(fechaAceptacion);
		nuevoUsuario.setFechaAceptacionPrivacidad(fechaAceptacion);
		nuevoUsuario.setVersionTerminos(VERSION_TERMINOS_ACTUAL);
		nuevoUsuario.setVersionPoliticaPrivacidad(VERSION_PRIVACIDAD_ACTUAL);

		// Valores iniciales para manejo de bloqueo
		nuevoUsuario.setIntentosFallidos(0);
		nuevoUsuario.setCuentaBloqueada(false);
		nuevoUsuario.setFechaBloqueo(null);

		return usuarioRepository.save(nuevoUsuario);
	}

	// Cambia el rol de un usuario; la autorización debe validarse en el BFF
	public UsuarioModel cambiarRol(Long id, String nuevoRol) {
		if (id == null) {
			throw new IllegalArgumentException("El id es obligatorio");
		}

		if (nuevoRol == null || nuevoRol.isBlank()) {
			throw new IllegalArgumentException("El rol es obligatorio");
		}

		String factoryKey = nuevoRol.trim().toLowerCase();

		UsuarioModel usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		Usuario tipoUsuario = usuarioFactory.obtenerUsuario(factoryKey);
		tipoUsuario.crearUsuario(usuario);

		return usuarioRepository.save(usuario);
	}

	private boolean coincideConContrasena(String contrasenaIngresada, String contrasenaGuardada) {
		if (contrasenaIngresada == null || contrasenaGuardada == null) {
			return false;
		}

		try {
			if (passwordEncoder.matches(contrasenaIngresada, contrasenaGuardada)) {
				return true;
			}
		} catch (IllegalArgumentException ignored) {
			// La contraseña guardada no tiene formato BCrypt;
			// se intenta compatibilidad con texto plano.
		}

		return contrasenaGuardada.equals(contrasenaIngresada);
	}

	// Validar credenciales para login desde el BFF
	public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {

		if (requestInvalido(request)) {
			return credencialesInvalidas("Correo y contraseña son obligatorios");
		}

		String correoNormalizado = request.correo().trim().toLowerCase();

		Optional<UsuarioModel> usuarioOptional = usuarioRepository.findByCorreoIgnoreCase(correoNormalizado);

		if (usuarioOptional.isEmpty()) {
			return credencialesInvalidas("Credenciales inválidas");
		}

		UsuarioModel usuario = usuarioOptional.get();

		if (usuario.isCuentaBloqueada()) {
			Optional<ValidarCredencialesResponse> respuestaBloqueo = manejarCuentaBloqueada(usuario);

			if (respuestaBloqueo.isPresent()) {
				return respuestaBloqueo.get();
			}
		}

		if (!coincideConContrasena(request.contrasena(), usuario.getContrasena())) {
			return manejarIntentoFallido(usuario);
		}

		return manejarLoginExitoso(usuario);
	}

	// Login que retorna JWT directamente (para uso sin BFF)
	public LoginResponse login(ValidarCredencialesRequest request) {
		if (requestInvalido(request)) {
			throw new IllegalArgumentException("Correo y contraseña son obligatorios");
		}

		String correoNormalizado = request.correo().trim().toLowerCase();

		UsuarioModel usuario = usuarioRepository.findByCorreoIgnoreCase(correoNormalizado)
				.orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

		if (usuario.isCuentaBloqueada()) {
			LocalDateTime fechaDesbloqueo = usuario.getFechaBloqueo() != null
					? usuario.getFechaBloqueo().plusMinutes(DURACION_BLOQUEO_MINUTOS)
					: null;

			if (fechaDesbloqueo == null || LocalDateTime.now().isBefore(fechaDesbloqueo)) {
				throw new IllegalArgumentException("Cuenta bloqueada temporalmente. Intente nuevamente en 15 minutos.");
			}

			usuario.setCuentaBloqueada(false);
			usuario.setIntentosFallidos(0);
			usuario.setFechaBloqueo(null);
			usuarioRepository.save(usuario);
		}

		if (!coincideConContrasena(request.contrasena(), usuario.getContrasena())) {
			usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

			if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
				usuario.setCuentaBloqueada(true);
				usuario.setFechaBloqueo(LocalDateTime.now());
				usuarioRepository.save(usuario);
				throw new IllegalArgumentException("Cuenta bloqueada por 15 minutos por demasiados intentos fallidos");
			}

			usuarioRepository.save(usuario);
			throw new IllegalArgumentException(
					"Credenciales inválidas. Intento " + usuario.getIntentosFallidos() + " de " + MAX_INTENTOS_FALLIDOS);
		}

		// Login exitoso: resetear intentos y generar token
		usuario.setIntentosFallidos(0);
		usuario.setCuentaBloqueada(false);
		usuario.setFechaBloqueo(null);
		usuarioRepository.save(usuario);

		String token = jwtService.generarToken(usuario.getCorreo(), usuario.getRol(), usuario.getId());

		return new LoginResponse(token, usuario.getId(), usuario.getCorreo(), usuario.getNombre(), usuario.getRol());
	}

	private boolean requestInvalido(ValidarCredencialesRequest request) {
		return request == null ||
				request.correo() == null || request.correo().isBlank() ||
				request.contrasena() == null || request.contrasena().isBlank();
	}

	private Optional<ValidarCredencialesResponse> manejarCuentaBloqueada(UsuarioModel usuario) {

		if (usuario.getFechaBloqueo() == null) {
			usuario.setFechaBloqueo(LocalDateTime.now());
			usuarioRepository.save(usuario);

			return Optional.of(respuestaBloqueada(
					usuario,
					"Cuenta bloqueada temporalmente. Intente nuevamente en 15 minutos."));
		}

		LocalDateTime fechaDesbloqueo = usuario.getFechaBloqueo().plusMinutes(DURACION_BLOQUEO_MINUTOS);

		if (LocalDateTime.now().isBefore(fechaDesbloqueo)) {
			return Optional.of(respuestaBloqueada(
					usuario,
					"Cuenta bloqueada temporalmente. Intente nuevamente en 15 minutos."));
		}

		// Si ya pasaron los 15 minutos, se desbloquea automáticamente
		usuario.setCuentaBloqueada(false);
		usuario.setIntentosFallidos(0);
		usuario.setFechaBloqueo(null);
		usuarioRepository.save(usuario);

		return Optional.empty();
	}

	// Incrementa los intentos fallidos y bloquea la cuenta si se supera el límite
	private ValidarCredencialesResponse manejarIntentoFallido(UsuarioModel usuario) {

		usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

		if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
			usuario.setCuentaBloqueada(true);
			usuario.setFechaBloqueo(LocalDateTime.now());
			usuarioRepository.save(usuario);

			return respuestaBloqueada(
					usuario,
					"Cuenta bloqueada por 15 minutos por demasiados intentos fallidos");
		}

		usuarioRepository.save(usuario);

		return credencialesInvalidas(
				"Credenciales inválidas. Intento "
						+ usuario.getIntentosFallidos()
						+ " de "
						+ MAX_INTENTOS_FALLIDOS);
	}

	// Resetea los intentos fallidos y desbloquea la cuenta después de un login
	// exitoso
	private ValidarCredencialesResponse manejarLoginExitoso(UsuarioModel usuario) {

		usuario.setIntentosFallidos(0);
		usuario.setCuentaBloqueada(false);
		usuario.setFechaBloqueo(null);
		usuarioRepository.save(usuario);

		return new ValidarCredencialesResponse(
				true,
				usuario.getId(),
				usuario.getCorreo(),
				usuario.getNombre(),
				usuario.getRol(),
				"Credenciales válidas");
	}

	// Respuesta común para credenciales inválidas, con mensaje personalizado según
	// el contexto
	private ValidarCredencialesResponse credencialesInvalidas(String mensaje) {
		return new ValidarCredencialesResponse(
				false,
				null,
				null,
				null,
				null,
				mensaje);
	}

	// Respuesta común para cuentas bloqueadas, con mensaje personalizado según el
	// contexto
	private ValidarCredencialesResponse respuestaBloqueada(UsuarioModel usuario, String mensaje) {
		return new ValidarCredencialesResponse(
				false,
				usuario.getId(),
				usuario.getCorreo(),
				usuario.getNombre(),
				usuario.getRol(),
				mensaje);
	}

	// Eliminar usuario por id
	public boolean eliminar(Long id) {
		if (!usuarioRepository.existsById(id)) {
			return false;
		}

		usuarioRepository.deleteById(id);
		return true;
	}

}
