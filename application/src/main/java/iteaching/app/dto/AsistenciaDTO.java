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
public class AsistenciaDTO {
    private Long id;
    @NotBlank(message = "La fecha es obligatoria")
    private String fecha;
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
    private String observacion;
    @NotNull(message = "El estudiante es obligatorio")
    private Long estudianteId;
    private String estudianteNombre;
    @NotNull(message = "La asignatura es obligatoria")
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long registradoPorId;
    private String registradoPorNombre;
}
