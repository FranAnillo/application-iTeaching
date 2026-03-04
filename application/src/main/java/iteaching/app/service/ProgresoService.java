package iteaching.app.service;

import iteaching.app.Models.*;
import iteaching.app.dto.ProgresoDTO;
import iteaching.app.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProgresoService {

    private final AsignaturaRepository asignaturaRepository;
    private final TareaRepository tareaRepository;
    private final EntregaRepository entregaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PersonaRepository personaRepository;

    public ProgresoService(AsignaturaRepository asignaturaRepository,
                           TareaRepository tareaRepository,
                           EntregaRepository entregaRepository,
                           AsistenciaRepository asistenciaRepository,
                           PersonaRepository personaRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.tareaRepository = tareaRepository;
        this.entregaRepository = entregaRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.personaRepository = personaRepository;
    }

    public ProgresoDTO getProgresoEstudiante(Long estudianteId, Long asignaturaId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
            .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        List<Tarea> tareas = tareaRepository.findByAsignaturaId(asignaturaId);
        int totalTareas = tareas.size();
        int tareasEntregadas = 0;
        int tareasCalificadas = 0;
        double sumaCalificaciones = 0;

        for (Tarea tarea : tareas) {
            for (Entrega entrega : tarea.getEntregas()) {
                if (entrega.getEstudiante().getId().equals(estudianteId)) {
                    tareasEntregadas++;
                    if (entrega.getCalificacion() != null) {
                        tareasCalificadas++;
                        sumaCalificaciones += entrega.getCalificacion();
                    }
                    break;
                }
            }
        }

        long totalAsistencias = asistenciaRepository.countTotal(estudianteId, asignaturaId);
        long asistenciasPresente = asistenciaRepository.countPresentes(estudianteId, asignaturaId);
        double porcentajeAsistencia = totalAsistencias > 0 ? (asistenciasPresente * 100.0 / totalAsistencias) : 100.0;
        double promedioCalif = tareasCalificadas > 0 ? sumaCalificaciones / tareasCalificadas : 0.0;
        double porcentajeProgreso = totalTareas > 0 ? (tareasEntregadas * 100.0 / totalTareas) : 0.0;

        ProgresoDTO dto = new ProgresoDTO();
        dto.setAsignaturaId(asignaturaId);
        dto.setAsignaturaNombre(asignatura.getNombre());
        dto.setTotalTareas(totalTareas);
        dto.setTareasEntregadas(tareasEntregadas);
        dto.setTareasCalificadas(tareasCalificadas);
        dto.setPromedioCalificaciones(Math.round(promedioCalif * 100.0) / 100.0);
        dto.setTotalClases(totalAsistencias);
        dto.setClasesAsistidas(asistenciasPresente);
        dto.setPorcentajeAsistencia(Math.round(porcentajeAsistencia * 100.0) / 100.0);
        dto.setPorcentajeProgreso(Math.round(porcentajeProgreso * 100.0) / 100.0);
        return dto;
    }

    public List<ProgresoDTO> getProgresoGlobal(String username) {
        Persona persona = personaRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<Asignatura> asignaturas = persona.getAsignaturasInscritas();
        List<ProgresoDTO> resultados = new ArrayList<>();

        for (Asignatura asig : asignaturas) {
            resultados.add(getProgresoEstudiante(persona.getId(), asig.getId()));
        }

        return resultados;
    }
}
