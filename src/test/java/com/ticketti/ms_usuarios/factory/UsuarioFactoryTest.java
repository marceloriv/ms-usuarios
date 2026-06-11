package com.ticketti.ms_usuarios.factory;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ticketti.ms_usuarios.model.UsuarioModel;
import com.ticketti.ms_usuarios.usuarios.UsuarioAdmin;
import com.ticketti.ms_usuarios.usuarios.UsuarioCliente;
import com.ticketti.ms_usuarios.usuarios.UsuarioOrganizador;

class UsuarioFactoryTest {

	private UsuarioFactory usuarioFactory;
	private UsuarioAdmin usuarioAdmin;
	private UsuarioCliente usuarioCliente;
	private UsuarioOrganizador usuarioOrganizador;

	@BeforeEach
	void setUp() {
		usuarioAdmin = new UsuarioAdmin();
		usuarioCliente = new UsuarioCliente();
		usuarioOrganizador = new UsuarioOrganizador();
		usuarioFactory = new UsuarioFactory(Map.of(
				"admin", usuarioAdmin,
				"cliente", usuarioCliente,
				"organizador", usuarioOrganizador));
	}

	@Test
	void obtenerUsuarioAdminDevuelveLaImplementacionCorrecta() {
		assertSame(usuarioAdmin, usuarioFactory.obtenerUsuario("ADMIN"));
	}

	@Test
	void obtenerUsuarioClienteDevuelveLaImplementacionCorrecta() {
		assertSame(usuarioCliente, usuarioFactory.obtenerUsuario("cliente"));
	}

	@Test
	void obtenerUsuarioOrganizadorDevuelveLaImplementacionCorrecta() {
		assertSame(usuarioOrganizador, usuarioFactory.obtenerUsuario("Organizador"));
	}

	@Test
	void obtenerUsuarioConTipoInvalidoLanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> usuarioFactory.obtenerUsuario("desconocido"));
	}

	@Test
	void usuarioClienteAsignaRolCliente() {
		UsuarioModel usuario = nuevoUsuario();

		usuarioCliente.crearUsuario(usuario);

		assertEquals("CLIENTE", usuario.getRol());
	}

	@Test
	void usuarioOrganizadorAsignaRolOrganizador() {
		UsuarioModel usuario = nuevoUsuario();

		usuarioOrganizador.crearUsuario(usuario);

		assertEquals("ORGANIZADOR", usuario.getRol());
	}

	private UsuarioModel nuevoUsuario() {
		UsuarioModel usuario = new UsuarioModel();
		usuario.setNombre("Ana");
		usuario.setCorreo("ana@example.com");
		usuario.setRol("admin");
		usuario.setTelefono("123456789");
		usuario.setDireccion("Calle Principal 123");
		usuario.setContrasena("Secret123");
		return usuario;
	}
}