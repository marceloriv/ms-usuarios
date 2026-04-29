package com.ticketti.ms_usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//modelo de datos que representa a un usuario en el sistema, con anotaciones de JPA 
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Data
public class UsuarioModel extends User {

	@NotBlank(message = "La contraseña no puede ser nula")
	@Column(name = "contrasena", nullable = false, length = 120)

	//para que la pass solo se registre en la bd ,pero no lo muestra en las solucitudes get

	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String contrasena;

	

}
