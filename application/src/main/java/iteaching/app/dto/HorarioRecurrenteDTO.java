package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorarioRecurrenteDTO {
    private Long id;
    private String titulo;
    private String aula;
    private String diaSemana; // e.g. "MONDAY"
    private String horaInicio; // HH:mm
    private String horaFin;    // HH:mm
    private String fechaInicio; // yyyy-MM-dd
    private String fechaFin;    // yyyy-MM-dd
    private int frecuenciaSemanas;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long grupoId;
    private String grupoNombre;
    private Long profesorId;
    private String profesorNombre;
}
