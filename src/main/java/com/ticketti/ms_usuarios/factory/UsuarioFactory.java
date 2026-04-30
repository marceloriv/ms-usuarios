package com.ticketti.ms_usuarios.factory;

import com.ticketti.ms_usuarios.usuarios.Usuario;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class UsuarioFactory {
    

    private final Map<String, Usuario> tiposUsuarios;

    public Usuario obtenerUsuario(String tipo){
        Usuario usu = tiposUsuarios.get(tipo.toLowerCase());
        if (usu == null) {
            throw new IllegalArgumentException("Tipo de usuario no válido");
        }
        System.out.println("Se creó el usuario: " + usu);
        return usu;
    }

}
