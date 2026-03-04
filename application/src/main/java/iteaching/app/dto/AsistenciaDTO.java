package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsistenciaDTO {
    private Long id;
    private String fecha;
    private String estado;
    private String observacion;
    private Long estudianteId;
    private String estudianteNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long registradoPorId;
    private String registradoPorNombre;
}
