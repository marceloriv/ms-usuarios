package com.ticketti.ms_usuarios.usuarios;

import org.springframework.stereotype.Component;

import com.ticketti.ms_usuarios.model.UsuarioModel;

@Component("admin")
public class UsuarioAdmin extends Usuario {

    @Override
    public void crearUsuario(UsuarioModel usuarioModel) {
        usuarioModel.setRol("ADMIN");
        System.out.println("Usuario admin creado con éxito: " + usuarioModel);
    }
}


