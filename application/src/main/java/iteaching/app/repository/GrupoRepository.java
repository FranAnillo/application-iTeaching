package iteaching.app.repository;

import iteaching.app.Models.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByAsignaturaId(Long asignaturaId);
    List<Grupo> findByAsignaturaIdAndTipo(Long asignaturaId, Grupo.TipoGrupo tipo);
}
