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
import lombok.Getter;
import lombok.Setter;
//el factory method lo debo dejar en el service y de ahí crear las carpetas 



@MappedSuperclass
@Getter
@Setter
public abstract class User {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank(message = "El nombre debe ser obligatorio")
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
	@Size(min = 10, max = 255, message = "La dirección debe tener entre 10 y 255 caracteres")
    @Column(length = 255)
    private String direccion;

}
