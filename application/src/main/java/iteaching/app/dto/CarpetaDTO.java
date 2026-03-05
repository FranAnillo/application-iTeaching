package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarpetaDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String nombre;
    @NotNull
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long padreId;
    private String padreNombre;
}
