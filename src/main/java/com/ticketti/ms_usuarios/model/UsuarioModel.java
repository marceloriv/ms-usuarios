package com.ticketti.ms_usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

//modelo de datos que representa a un usuario en el sistema, con anotaciones de JPA 
@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class UsuarioModel  extends User {
	

	@Column(name = "contrasena", nullable = false, length = 120)

	//para que la pass solo se registre en la bd ,pero no lo muestra en las solucitudes get

	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String contrasena;

	

}
