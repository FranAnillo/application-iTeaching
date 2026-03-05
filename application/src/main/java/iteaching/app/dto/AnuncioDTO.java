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
public class AnuncioDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String titulo;
    @NotBlank
    @Size(max = 10000)
    private String contenido;
    private String fechaCreacion;
    private Boolean importante;
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long autorId;
    private String autorNombre;
}
