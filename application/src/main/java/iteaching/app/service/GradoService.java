package iteaching.app.service;

import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Grado;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.GradoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import iteaching.app.dto.GradoDTO;

@Service
public class GradoService {

    private final GradoRepository gradoRepository;
    private final AsignaturaRepository asignaturaRepository;

    public GradoService(GradoRepository gradoRepository, AsignaturaRepository asignaturaRepository) {
        this.gradoRepository = gradoRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<GradoDTO> findAll() {
        return gradoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public GradoDTO save(GradoDTO dto) {
        Grado g = new Grado();
        g.setNombre(dto.getNombre());
        g.setCursoAcademico(dto.getCursoAcademico());
        g.setCentroImparticion(dto.getCentroImparticion());
        Grado saved = gradoRepository.save(g);
        return toDTO(saved);
    }

    public GradoDTO findById(Long id) {
        Grado g = gradoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado: " + id));
        return toDTO(g);
    }

    // El método getAsignaturas debe ajustarse en el modelo, pero se omite aquí para simplificar.

    // La vinculación de asignaturas se hará al crear la asignatura, no desde Grado.

    // La desvinculación de asignaturas se hará desde Asignatura.
    private GradoDTO toDTO(Grado g) {
        GradoDTO dto = new GradoDTO();
        dto.setId(g.getId());
        dto.setNombre(g.getNombre());
        dto.setCursoAcademico(g.getCursoAcademico());
        dto.setCentroImparticion(g.getCentroImparticion());
        return dto;
    }

    /**
     * Asocia una asignatura existente al grado indicado.
     * Si la asignatura ya estaba vinculada a otro grado, se sobrescribe.
     */
    @Transactional
    public GradoDTO addAsignatura(Long gradoId, Long asignaturaId) {
        Grado grado = gradoRepository.findById(gradoId)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado: " + gradoId));
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada: " + asignaturaId));

        asignatura.setGrado(grado);
        asignaturaRepository.save(asignatura);

        return toDTO(grado);
    }

    /**
     * Devuelve las asignaturas que pertenecen al grado.
     * Utiliza un método de consulta del repositorio para no depender de la
     * relación bidireccional en el modelo.
     */
    public List<Asignatura> getAsignaturas(Long gradoId) {
        return asignaturaRepository.findByGradoId(gradoId);
    }

    /**
     * Elimina la asociación entre un grado y una asignatura.
     * Si la asignatura no está ligada al grado indicado no hace nada.
     */
    @Transactional
    public GradoDTO removeAsignatura(Long gradoId, Long asignaturaId) {
        Grado grado = gradoRepository.findById(gradoId)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado: " + gradoId));
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada: " + asignaturaId));

        if (asignatura.getGrado() != null && gradoId.equals(asignatura.getGrado().getId())) {
            asignatura.setGrado(null);
            asignaturaRepository.save(asignatura);
        }
        return toDTO(grado);
    }
}
