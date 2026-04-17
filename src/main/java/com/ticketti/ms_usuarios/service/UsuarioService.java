package com.ticketti.ms_usuarios.service;

import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
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

	public boolean eliminar(Long id) {
		if (!usuarioRepository.existsById(id)) {
			return false;
		}
		usuarioRepository.deleteById(id);
		return true;
	}

}
