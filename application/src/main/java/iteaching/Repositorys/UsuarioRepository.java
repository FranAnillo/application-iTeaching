package iteaching.Repositorys;

import org.springframework.data.jpa.repository.JpaRepository;

import iteaching.app.Models.Usuarios;

public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {
    Usuarios findByUsername(String username);
}
