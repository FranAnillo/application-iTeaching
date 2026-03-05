package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255)
    private String nombre;
    @Size(max = 2000)
    private String descripcion;
    @Size(max = 2048)
    private String url;
    private Long creadorId;
    private String creadorNombre;
    private Set<Long> profesorIds;
    private Set<Long> estudianteIds;
}
