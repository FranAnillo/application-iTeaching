package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeDTO {
    private Long id;
    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 5000, message = "El contenido no puede superar los 5000 caracteres")
    private String contenido;
    private String fechaEnvio;
    private Boolean leido;
    private Long remitenteId;
    private String remitenteNombre;
    @NotNull(message = "El destinatario es obligatorio")
    private Long destinatarioId;
    private String destinatarioNombre;
    private Long asignaturaId;
    private String asignaturaNombre;
}
