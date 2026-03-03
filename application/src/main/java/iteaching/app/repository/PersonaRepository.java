package iteaching.app.repository;

import iteaching.app.Models.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByEmail(String email);
    Optional<Persona> findByUsername(String username);
    boolean existsByEmail(String email);
    List<Persona> findByNombreContainingIgnoreCase(String nombre);
    List<Persona> findByApellidoContainingIgnoreCase(String apellido);
}
