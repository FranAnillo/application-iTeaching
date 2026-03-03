package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaDTO {
    private Long id;
    @NotBlank
    private String nombre;
    private String descripcion;
    private Set<Long> estudianteIds;
}
