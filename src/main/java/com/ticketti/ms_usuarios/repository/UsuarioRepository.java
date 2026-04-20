package com.ticketti.ms_usuarios.repository;

import com.ticketti.ms_usuarios.model.UsuarioModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

//para manejar la persitencia en la bd y realizar operaciones CRUD 
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

	Optional<UsuarioModel> findByCorreo(String correo);


}
