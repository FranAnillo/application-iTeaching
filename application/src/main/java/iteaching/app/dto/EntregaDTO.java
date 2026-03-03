package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntregaDTO {
    private Long id;
    private String contenido;
    private String urlAdjunto;
    private String fechaEntrega;
    private Double calificacion;
    private String comentarioProfesor;
    private Long tareaId;
    private String tareaTitulo;
    private Long estudianteId;
    private String estudianteNombre;
}
