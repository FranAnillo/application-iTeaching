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
public class EntregaDTO {
    private Long id;
    @NotBlank(message = "El contenido de la entrega no puede estar vacío")
    @Size(max = 10000, message = "El contenido no puede superar los 10000 caracteres")
    private String contenido;
    @Size(max = 2048, message = "La URL del adjunto no puede superar los 2048 caracteres")
    private String urlAdjunto;
    private String fechaEntrega;
    private Double calificacion;
    private String comentarioProfesor;
    @NotNull(message = "La tarea es obligatoria")
    private Long tareaId;
    private String tareaTitulo;
    private Long estudianteId;
    private String estudianteNombre;
}
