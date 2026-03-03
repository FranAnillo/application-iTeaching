package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaDTO {
    private Long id;
    @NotBlank
    private String titulo;
    private String descripcion;
    private String fechaCreacion;
    @NotNull
    private String fechaEntrega;
    private Double puntuacionMaxima;
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long creadorId;
    private String creadorNombre;
    private int totalEntregas;
}
