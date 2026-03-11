package iteaching.app.service;

import iteaching.app.Models.*;
import iteaching.app.dto.HorarioRecurrenteDTO;
import iteaching.app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    private final HorarioRecurrenteRepository horarioRepository;
    private final ClaseRepository claseRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;

    public HorarioService(HorarioRecurrenteRepository horarioRepository,
                          ClaseRepository claseRepository,
                          AsignaturaRepository asignaturaRepository,
                          GrupoRepository grupoRepository,
                          PersonaRepository personaRepository) {
        this.horarioRepository = horarioRepository;
        this.claseRepository = claseRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.grupoRepository = grupoRepository;
        this.personaRepository = personaRepository;
    }

    public List<HorarioRecurrenteDTO> findByAsignatura(Long asignaturaId) {
        return horarioRepository.findByAsignaturaId(asignaturaId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public HorarioRecurrenteDTO createAndGenerate(HorarioRecurrenteDTO dto) {
        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Grupo grupo = dto.getGrupoId() != null 
                ? grupoRepository.findById(dto.getGrupoId()).orElse(null)
                : null;
        Persona profesor = personaRepository.findById(dto.getProfesorId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        HorarioRecurrente rule = new HorarioRecurrente();
        rule.setTitulo(dto.getTitulo());
        rule.setAula(dto.getAula());
        rule.setDiaSemana(DayOfWeek.valueOf(dto.getDiaSemana()));
        rule.setHoraInicio(LocalTime.parse(dto.getHoraInicio()));
        rule.setHoraFin(LocalTime.parse(dto.getHoraFin()));
        rule.setFechaInicio(LocalDate.parse(dto.getFechaInicio()));
        rule.setFechaFin(LocalDate.parse(dto.getFechaFin()));
        rule.setFrecuenciaSemanas(dto.getFrecuenciaSemanas() > 0 ? dto.getFrecuenciaSemanas() : 1);
        rule.setAsignatura(asignatura);
        rule.setGrupo(grupo);
        rule.setProfesor(profesor);

        HorarioRecurrente saved = horarioRepository.save(rule);
        
        // AUTO-GENERATE CLASSES
        generateClasesFromRule(saved);
        
        return toDTO(saved);
    }

    private void generateClasesFromRule(HorarioRecurrente rule) {
        LocalDate current = rule.getFechaInicio();
        
        // Find first occurrence of the day of week
        while (current.getDayOfWeek() != rule.getDiaSemana() && !current.isAfter(rule.getFechaFin())) {
            current = current.plusDays(1);
        }

        while (!current.isAfter(rule.getFechaFin())) {
            Clase session = new Clase();
            session.setTitulo(rule.getTitulo());
            session.setAula(rule.getAula());
            session.setHoraComienzo(current.atTime(rule.getHoraInicio()));
            session.setHoraFin(current.atTime(rule.getHoraFin()));
            session.setAsignatura(rule.getAsignatura());
            session.setGrupo(rule.getGrupo());
            session.setProfesor(rule.getProfesor());
            session.setRecurrenteId(rule.getId());
            session.setEstadoClase(EstadoClase.ACEPTADA);
            session.setAceptacionAlumno(true);
            session.setAceptacionProfesor(true);
            
            claseRepository.save(session);
            
            current = current.plusWeeks(rule.getFrecuenciaSemanas());
        }
    }

    @Transactional
    public void delete(Long id) {
        // Optionially delete generated classes here if needed
        horarioRepository.deleteById(id);
    }

    private HorarioRecurrenteDTO toDTO(HorarioRecurrente h) {
        HorarioRecurrenteDTO dto = new HorarioRecurrenteDTO();
        dto.setId(h.getId());
        dto.setTitulo(h.getTitulo());
        dto.setAula(h.getAula());
        dto.setDiaSemana(h.getDiaSemana().name());
        dto.setHoraInicio(h.getHoraInicio().toString());
        dto.setHoraFin(h.getHoraFin().toString());
        dto.setFechaInicio(h.getFechaInicio().toString());
        dto.setFechaFin(h.getFechaFin().toString());
        dto.setFrecuenciaSemanas(h.getFrecuenciaSemanas());
        dto.setAsignaturaId(h.getAsignatura().getId());
        dto.setAsignaturaNombre(h.getAsignatura().getNombre());
        if (h.getGrupo() != null) {
            dto.setGrupoId(h.getGrupo().getId());
            dto.setGrupoNombre(h.getGrupo().getNombre());
        }
        if (h.getProfesor() != null) {
            dto.setProfesorId(h.getProfesor().getId());
            dto.setProfesorNombre(h.getProfesor().getNombreCompleto());
        }
        return dto;
    }
}
