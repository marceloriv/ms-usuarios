package com.ticketti.ms_usuarios.dto;

public record ValidarCredencialesResponse(
        boolean valido,
        Long usuarioId,
        String correo,
        String nombre,
        String rol,
        String mensaje
) {
}