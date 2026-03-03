package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarpetaDTO {
    private Long id;
    @NotBlank
    private String nombre;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Long padreId;
    private String padreNombre;
}
