package com.ticketti.ms_usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
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

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, unique = true, length = 120)
	private String correo;

	@Column(nullable = false, length = 50)
	private String rol;

	@Column(nullable = false, length = 9)
    private String telefono;

    @Column(length = 255)
    private String direccion;

}
