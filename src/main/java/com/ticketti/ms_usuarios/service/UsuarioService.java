package com.ticketti.ms_usuarios.service;

import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

	// patrones para validar formato de correo y contrasena segura
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern PASSWORD_PATTERN =
			Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

	// servicio que contiene la lógica de negocio
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder contrasenaEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder contrasenaEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.contrasenaEncoder = contrasenaEncoder;
	}

	public List<UsuarioModel> listar() {
		return usuarioRepository.findAll();
	}

	public Optional<UsuarioModel> obtenerPorId(Long id) {
		return usuarioRepository.findById(id);
	}

	public Optional<UsuarioModel> obtenerPorCorreo(String correo) {
		return usuarioRepository.findByCorreo(correo);
	}

	public UsuarioModel crear(UsuarioModel usuario) {
		usuario.setId(null);
		return usuarioRepository.save(usuario);
	}

	// actualizar los usuarios del sistema uwu
	public Optional<UsuarioModel> actualizar(Long id, UsuarioModel usuarioActualizado) {
		return usuarioRepository.findById(id)
				.map(usuarioExistente -> {
					usuarioExistente.setNombre(usuarioActualizado.getNombre());
					usuarioExistente.setCorreo(usuarioActualizado.getCorreo());
					usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
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

		if (nuevoUsuario.getNombre() == null || nuevoUsuario.getNombre().isBlank()) {
			throw new IllegalArgumentException("El nombre es obligatorio");
		}

		if (nuevoUsuario.getCorreo() == null || !EMAIL_PATTERN.matcher(nuevoUsuario.getCorreo()).matches()) {
			throw new IllegalArgumentException("El correo no tiene un formato valido");
		}

		String correoNormalizado = nuevoUsuario.getCorreo().trim().toLowerCase();

		if (usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
			throw new IllegalArgumentException("El correo ya existe");
		}

		if (nuevoUsuario.getContrasena() == null
				|| !PASSWORD_PATTERN.matcher(nuevoUsuario.getContrasena()).matches()) {
			throw new IllegalArgumentException(
					"La contrasena debe tener minimo 8 caracteres, mayuscula, minuscula y numero");
		}

		if (nuevoUsuario.getTelefono() == null || !nuevoUsuario.getTelefono().matches("\\d{9}")) {
			throw new IllegalArgumentException("El telefono debe tener exactamente 9 digitos y en formato numerico");
		}

		// se normaliza el correo a minusculas

		nuevoUsuario.setId(null);
		nuevoUsuario.setNombre(nuevoUsuario.getNombre().trim());
		nuevoUsuario.setCorreo(correoNormalizado);
		
		//se  codifica la contraseña antes de cuardarla en la base de datos y no se guarde en texto plano
		//aplicando el algoritmo de hashing bcrypt y spring security para generar un hash seguro de la contraseña del usuario
		if (nuevoUsuario.getContrasena() != null && !nuevoUsuario.getContrasena().trim().isEmpty()) {
			nuevoUsuario.setContrasena(contrasenaEncoder.encode(nuevoUsuario.getContrasena()));
		}

		return usuarioRepository.save(nuevoUsuario);
	}

	// eliminar usuario por id
	public boolean eliminar(Long id) {
		if (!usuarioRepository.existsById(id)) {
			return false;
		}
		usuarioRepository.deleteById(id);
		return true;
	}
}

