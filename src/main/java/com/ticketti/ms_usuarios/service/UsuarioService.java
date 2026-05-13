package com.ticketti.ms_usuarios.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ticketti.ms_usuarios.dto.ValidarCredencialesRequest;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesResponse;
import com.ticketti.ms_usuarios.factory.UsuarioFactory;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import com.ticketti.ms_usuarios.usuarios.Usuario;

@Service
public class UsuarioService {

	// patrones para validar formato de correo y contrasena segura
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern PASSWORD_PATTERN =
			Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

	// servicio que contiene la lógica de negocio
	private final UsuarioRepository usuarioRepository;
	private final UsuarioFactory usuarioFactory;
	private final PasswordEncoder passwordEncoder;


	public UsuarioService(UsuarioRepository usuarioRepository, UsuarioFactory usuarioFactory, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioFactory = usuarioFactory;
		this.passwordEncoder = passwordEncoder;
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

	public UsuarioModel crear(UsuarioModel usuario) {
		usuario.setId(null);
		return usuarioRepository.save(usuario);
	}

	// actualizar los usuarios del sistema uwu
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

	// Crear usuarios con validaciones de correo unico y formato correcto, contrasena segura
	// se utiliza .matches para validar el formato del correo y la contraseña
	// se lanza una excepcion IllegalArgumentException si este falla uwu
	public UsuarioModel crearUsuario(UsuarioModel nuevoUsuario) {
		if (nuevoUsuario == null) {
			throw new IllegalArgumentException("El usuario es obligatorio");
		}

		if (nuevoUsuario.getCorreo() == null || !EMAIL_PATTERN.matcher(nuevoUsuario.getCorreo()).matches()) {
			throw new IllegalArgumentException("El correo no tiene un formato valido");
		}

		String correoNormalizado = nuevoUsuario.getCorreo().trim().toLowerCase();

		if (usuarioRepository.findByCorreoIgnoreCase(correoNormalizado).isPresent()) {
			throw new IllegalArgumentException("El correo ya existe");
		}

		if (nuevoUsuario.getContrasena() == null
				|| !PASSWORD_PATTERN.matcher(nuevoUsuario.getContrasena()).matches()) {
			throw new IllegalArgumentException(
					"La contrasena debe tener minimo 8 caracteres, mayuscula, minuscula y numero");
		}

		// se utiliza el factory para crear el usuario con el rol correspondiente
		Usuario tipoUsuario = usuarioFactory.obtenerUsuario(nuevoUsuario.getRol());
		tipoUsuario.crearUsuario(nuevoUsuario);





		// se normaliza el correo a minusculas

		nuevoUsuario.setId(null);
		nuevoUsuario.setNombre(nuevoUsuario.getNombre().trim());
		nuevoUsuario.setCorreo(correoNormalizado);
		nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));


		return usuarioRepository.save(nuevoUsuario);
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
			// La contraseña guardada no tiene formato BCrypt; se intenta compatibilidad con texto plano.
		}

		return contrasenaGuardada.equals(contrasenaIngresada);
	}

	// validar credenciales para login desde el BFF
	public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {

		if (request == null ||
				request.correo() == null || request.correo().isBlank() ||
				request.contrasena() == null || request.contrasena().isBlank()) {
			return credencialesInvalidas("Correo y contraseña son obligatorios");
		}

		String correoNormalizado = request.correo().trim().toLowerCase();

		return usuarioRepository.findByCorreoIgnoreCase(correoNormalizado)
				.filter(usuario -> coincideConContrasena(request.contrasena(), usuario.getContrasena()))
				.map(usuario -> new ValidarCredencialesResponse(
						true,
						usuario.getId(),
						usuario.getCorreo(),
						usuario.getNombre(),
						usuario.getRol(),
						"Credenciales válidas"
				))
				.orElseGet(() -> credencialesInvalidas("Credenciales inválidas"));
	}

	private ValidarCredencialesResponse credencialesInvalidas(String mensaje) {
		return new ValidarCredencialesResponse(
				false,
				null,
				null,
				null,
				null,
				mensaje
		);
	}

	// eliminar usuario por id
	public boolean eliminar(Long id) {
		if (!usuarioRepository.existsById(id)) {
			return false;
		}
		usuarioRepository.deleteById(id);
		return true;
	}

	// validar credenciales del usuario para autenticacion
	public boolean validarCredenciales(String correo, String contrasena) {
		if (correo == null || contrasena == null) {
			return false;
		}
		return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
				.map(usuario -> coincideConContrasena(contrasena, usuario.getContrasena()))
				.orElse(false);
	}
}

