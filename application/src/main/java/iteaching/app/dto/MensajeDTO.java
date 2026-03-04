package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeDTO {
    private Long id;
    private String contenido;
    private String fechaEnvio;
    private Boolean leido;
    private Long remitenteId;
    private String remitenteNombre;
    private Long destinatarioId;
    private String destinatarioNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
}
