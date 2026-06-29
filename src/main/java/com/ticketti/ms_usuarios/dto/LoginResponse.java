package com.ticketti.ms_usuarios.dto;

public record LoginResponse(
        String token,
        Long usuarioId,
        String correo,
        String nombre,
        String rol
) {
}
