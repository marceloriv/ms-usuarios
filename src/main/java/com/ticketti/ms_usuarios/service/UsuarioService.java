package com.ticketti.ms_usuarios.service;

import com.ticketti.ms_usuarios.factory.UsuarioFactory;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import com.ticketti.ms_usuarios.usuarios.Usuario;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesRequest;
import com.ticketti.ms_usuarios.dto.ValidarCredencialesResponse;



import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
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
	private final UsuarioFactory usuarioFactory;
	

	public UsuarioService(UsuarioRepository usuarioRepository, UsuarioFactory usuarioFactory) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioFactory = usuarioFactory;
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

		if (usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
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
		

		//la base de datos debe encriptar la contraseña no spring securty 
	

		return usuarioRepository.save(nuevoUsuario);
	}

	// cambia el rol de un usuario; la autorización debe validarse en el BFF


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

	// validar credenciales para login desde el BFF
	public ValidarCredencialesResponse validarCredenciales(ValidarCredencialesRequest request) {

		if (request == null ||
				request.correo() == null || request.correo().isBlank() ||
				request.contrasena() == null || request.contrasena().isBlank()) {
			return credencialesInvalidas("Correo y contraseña son obligatorios");
		}

		String correoNormalizado = request.correo().trim().toLowerCase();

		return usuarioRepository.findByCorreo(correoNormalizado)
				.filter(usuario -> usuario.getContrasena().equals(request.contrasena()))
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
}

