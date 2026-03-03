package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaseDTO {
    private Long id;
    private String horaComienzo;
    private String horaFin;
    private Boolean aceptacionAlumno;
    private Boolean aceptacionProfesor;
    private String estadoClase;
    private Long alumnoId;
    private String alumnoNombre;
    private Long profesorId;
    private String profesorNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
}
