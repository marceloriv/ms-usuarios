package com.ticketti.ms_usuarios.dto;


public record ValidarCredencialesRequest(
        String correo,
        String contrasena
) {
}
