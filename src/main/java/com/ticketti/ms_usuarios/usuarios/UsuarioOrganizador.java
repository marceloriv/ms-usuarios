package com.ticketti.ms_usuarios.usuarios;

import org.springframework.stereotype.Component;

import com.ticketti.ms_usuarios.model.UsuarioModel;




@Component("organizador")
public class UsuarioOrganizador extends Usuario {

    @Override
    public void crearUsuario(UsuarioModel usuarioModel) {
        usuarioModel.setRol("ORGANIZADOR");
        System.out.println("Usuario organizador creado con éxito: " + usuarioModel);
    }

}
