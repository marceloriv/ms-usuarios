package com.ticketti.ms_usuarios.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.ticketti.ms_usuarios.model.UsuarioModel;

class UsuarioControllerAdviceTest {

	private final UsuarioControllerAdvice usuarioControllerAdvice = new UsuarioControllerAdvice();

	@Test
	void manejarExcepcionConvierteLosErroresEnUnaListaLegible() throws Exception {
		Method method = UsuarioControllerAdviceTest.class.getDeclaredMethod("dummy", UsuarioModel.class);
		MethodParameter parameter = new MethodParameter(method, 0);
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new UsuarioModel(), "usuario");
		bindingResult.addError(new FieldError("usuario", "correo", "El correo no puede ser nulo"));
		bindingResult.addError(new FieldError("usuario", "nombre", "El nombre debe ser obligatorio"));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

		ResponseEntity<List<String>> response = usuarioControllerAdvice.manejarExcepcion(exception);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("correo: El correo no puede ser nulo"));
		assertTrue(response.getBody().contains("nombre: El nombre debe ser obligatorio"));
	}

	@SuppressWarnings("unused")
	private void dummy(UsuarioModel usuario) {
	}
}