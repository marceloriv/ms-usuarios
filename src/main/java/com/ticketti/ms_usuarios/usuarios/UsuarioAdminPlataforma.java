package com.ticketti.ms_usuarios.usuarios;

import org.springframework.stereotype.Component;

import com.ticketti.ms_usuarios.model.UsuarioModel;


@Component("adminplataforma")
public class UsuarioAdminPlataforma extends Usuario {

    @Override
    public void crearUsuario(UsuarioModel usuarioModel) {
        usuarioModel.setRol("ADMINPLATAFORMA");
        System.out.println("Usuario administrador de plataforma creado con éxito: " + usuarioModel);
    }

}



