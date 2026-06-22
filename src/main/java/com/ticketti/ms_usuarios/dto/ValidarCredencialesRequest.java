package com.ticketti.ms_usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidarCredencialesRequest(
        @NotBlank(message = "El correo es requerido")
        @Email(message = "El correo debe ser válido")
        String correo,

        @NotBlank(message = "La contraseña es requerida")
        String contrasena

        
) {
}
