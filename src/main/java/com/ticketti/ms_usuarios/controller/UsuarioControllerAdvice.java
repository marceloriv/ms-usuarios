package com.ticketti.ms_usuarios.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UsuarioControllerAdvice {
    //manejador global de errores para capturar las excepciones del microservicio para spring
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> manejarExcepcion(MethodArgumentNotValidException e) {
        List<String> errores = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

    return ResponseEntity.badRequest().body(errores);
    }

}