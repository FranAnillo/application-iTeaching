package iteaching.app.repository;

import iteaching.app.Models.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    List<Estudiante> findByNombreContainingIgnoreCase(String nombre);
    List<Estudiante> findByApellidoContainingIgnoreCase(String apellido);
}
