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
public class TareaDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String titulo;
    @Size(max = 5000)
    private String descripcion;
    private String fechaCreacion;
    @NotNull
    private String fechaEntrega;
    private Double puntuacionMaxima;
    private String tipoTarea; // TAREA, EVALUACION, SIMULACRO
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long creadorId;
    private String creadorNombre;
    private int totalEntregas;
}
