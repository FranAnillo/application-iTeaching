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
public class MaterialDTO {
    private Long id;
    @NotBlank(message = "El título del material es obligatorio")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    private String titulo;
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;
    @Size(max = 2048, message = "La URL del recurso no puede superar los 2048 caracteres")
    private String urlRecurso;
    @NotBlank(message = "El tipo de material es obligatorio")
    private String tipo;
    private String fechaCreacion;
    private Long autorId;
    private String autorNombre;
    @NotNull(message = "La asignatura es obligatoria")
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long carpetaId;
    private String carpetaNombre;
}
