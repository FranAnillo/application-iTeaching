package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String urlRecurso;
    private String tipo;
    private String fechaCreacion;
    private Long autorId;
    private String autorNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long carpetaId;
    private String carpetaNombre;
}
