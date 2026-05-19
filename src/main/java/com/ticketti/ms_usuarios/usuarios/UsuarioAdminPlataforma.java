package com.ticketti.ms_usuarios.usuarios;

import org.springframework.stereotype.Component;

import com.ticketti.ms_usuarios.model.UsuarioModel;

@Component("administradorPlataforma")

public class UsuarioAdminPlataforma {


    public void crearUsuario(UsuarioModel usuarioModel) {
        usuarioModel.setRol("ADMINISTRADOR_PLATAFORMA");
        System.out.println("Usuario administrador de plataforma creado con éxito: " + usuarioModel);
    }

}
