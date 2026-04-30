package com.ticketti.ms_usuarios.usuarios;

import org.springframework.stereotype.Component;

import com.ticketti.ms_usuarios.model.UsuarioModel;



@Component("cliente")
public class UsuarioCliente extends Usuario {


    @Override

    public void crearUsuario(UsuarioModel usuarioModel) {
        usuarioModel.setRol("CLIENTE");
        System.out.println("Usuario cliente creado con éxito: " + usuarioModel);


        
    }

}
