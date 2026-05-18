package com.ticketti.ms_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import com.ticketti.ms_usuarios.factory.UsuarioFactory;
import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private UsuarioFactory usuarioFactory;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UsuarioService usuarioService;

	@BeforeEach
	void setUp() {
		usuarioService = new UsuarioService(usuarioRepository, usuarioFactory, passwordEncoder);
	}


	@Test
	void obtenerPorIdDevuelveElUsuarioCuandoExiste() {
		UsuarioModel usuario = crearUsuario(1L, "Ana", "ana@example.com", "Secret123", "ADMIN");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

		Optional<UsuarioModel> resultado = usuarioService.obtenerPorId(1L);

		assertTrue(resultado.isPresent());
		assertEquals("ana@example.com", resultado.get().getCorreo());
	}



	@Test
	void actualizarCuandoNoExisteDevuelveVacio() {
		UsuarioModel actualizado = crearUsuario(null, "Ana Maria", "ana.maria@example.com", "NewPass1", "ADMIN");
		when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

		Optional<UsuarioModel> resultado = usuarioService.actualizar(7L, actualizado);

		assertTrue(resultado.isEmpty());
		verify(usuarioRepository, never()).save(any());
	}


	@Test
	void crearUsuarioConCorreoInvalidoLanzaExcepcion() {
		UsuarioModel nuevo = crearUsuario(null, "Ana", "correo-invalido", "Secret123", "admin");

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> usuarioService.crearUsuario(nuevo));

		assertEquals("El correo no tiene un formato valido", exception.getMessage());
		verifyNoInteractions(usuarioRepository, usuarioFactory, passwordEncoder);
	}

	@Test
	void crearUsuarioConCorreoDuplicadoLanzaExcepcion() {
		UsuarioModel nuevo = crearUsuario(null, "Ana", "Ana@Example.com", "Secret123", "admin");
		when(usuarioRepository.findByCorreoIgnoreCase("ana@example.com"))
				.thenReturn(Optional.of(crearUsuario(1L, "Ana", "ana@example.com", "Secret123", "ADMIN")));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> usuarioService.crearUsuario(nuevo));

		assertEquals("El correo ya existe", exception.getMessage());
		verify(usuarioFactory, never()).obtenerUsuario(anyString());
		verify(passwordEncoder, never()).encode(anyString());
	}



	@Test
	void eliminarDevuelveFalseCuandoNoExiste() {
		when(usuarioRepository.existsById(3L)).thenReturn(false);

		boolean eliminado = usuarioService.eliminar(3L);

		assertFalse(eliminado);
		verify(usuarioRepository, never()).deleteById(any());
	}

	private UsuarioModel crearUsuario(Long id, String nombre, String correo, String contrasena, String rol) {
		UsuarioModel usuario = new UsuarioModel();
		usuario.setId(id);
		usuario.setNombre(nombre);
		usuario.setCorreo(correo);
		usuario.setContrasena(contrasena);
		usuario.setRol(rol);
		usuario.setTelefono("123456789");
		usuario.setDireccion("Calle Principal 123");
		return usuario;
	}
}