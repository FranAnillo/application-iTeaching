package iteaching.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoDTO {
    private Long id;
    @NotBlank
    private String nombre;
    private String tipo; // TEORIA or PRACTICA
    private boolean inscribible;
    private Long asignaturaId;
    private String asignaturaNombre;
    private Set<Long> miembroIds;
}
