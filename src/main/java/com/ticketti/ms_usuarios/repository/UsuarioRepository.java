package com.ticketti.ms_usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketti.ms_usuarios.model.UsuarioModel;

//para manejar la persitencia en la bd y realizar operaciones CRUD
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

	Optional<UsuarioModel> findByCorreo(String correo);

	Optional<UsuarioModel> findByCorreoIgnoreCase(String correo);


}
