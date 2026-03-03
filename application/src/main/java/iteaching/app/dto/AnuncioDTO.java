package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnuncioDTO {
    private Long id;
    @NotBlank
    private String titulo;
    @NotBlank
    private String contenido;
    private String fechaCreacion;
    private Boolean importante;
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long autorId;
    private String autorNombre;
}
