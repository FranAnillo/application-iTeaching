package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgresoDTO {
    private Long asignaturaId;
    private String asignaturaNombre;
    private int totalTareas;
    private int tareasEntregadas;
    private int tareasCalificadas;
    private double promedioCalificaciones;
    private long totalClases;
    private long clasesAsistidas;
    private double porcentajeAsistencia;
    private double porcentajeProgreso;
}
