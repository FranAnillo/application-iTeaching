package iteaching.app.repository;

import iteaching.app.Models.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByAutorId(Long autorId);
    List<Material> findByAsignaturaId(Long asignaturaId);
    List<Material> findByTituloContainingIgnoreCase(String titulo);
    List<Material> findByAutorUsername(String username);
}
