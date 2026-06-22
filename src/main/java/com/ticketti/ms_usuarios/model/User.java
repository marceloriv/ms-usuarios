package com.ticketti.ms_usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
//el factory method lo debo dejar en el service y de ahí crear las carpetas 

@MappedSuperclass
@Getter
@Setter
@Data
public abstract class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull(message = "El nombre debe ser obligatorio")
	@Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
	@Column(nullable = false, length = 100)
	private String nombre;

	@NotBlank(message = "El correo no puede ser nulo")

	@Column(nullable = false, unique = true, length = 120)
	private String correo;

	@NotBlank(message = "El rol no puede ser nulo")
	@Column(nullable = false, length = 50)
	private String rol;

	@NotBlank(message = "El teléfono no puede ser nulo")
	@Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener exactamente 9 dígitos")

	@Column(nullable = false, length = 9)
	private String telefono;

	@NotNull(message = "La dirección no puede ser nula")
	@Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
	@Column(length = 255)
	private String direccion;

	// aceptación de términos y condiciones
	@Column(name = "acepta_terminos", nullable = false)
	private boolean aceptaTerminos = false;

	@Column(name = "fecha_aceptacion_terminos")
	private LocalDateTime fechaAceptacionTerminos;

	@Column(name = "version_terminos", length = 20)
	private String versionTerminos;

	// aceptación de política de privacidad
	@Column(name = "acepta_privacidad", nullable = false)
	private boolean aceptaPrivacidad = false;

	@Column(name = "fecha_aceptacion_privacidad")
	private LocalDateTime fechaAceptacionPrivacidad;

	@Column(name = "version_politica_privacidad", length = 20)
	private String versionPoliticaPrivacidad;


	// se crearán campos en el model para manejar intentos fallido de login,
	// bloqueos,
	@Column(name = "intentos_fallidos", nullable = false)
	private int intentosFallidos = 0;

	@Column(name = "cuenta_bloqueada", nullable = false)
	private boolean cuentaBloqueada = false;
	// fecha del último intento fallido para calcular el tiempo de bloqueo
	@Column(name = "fecha_bloqueo")
	private LocalDateTime fechaBloqueo;

}
