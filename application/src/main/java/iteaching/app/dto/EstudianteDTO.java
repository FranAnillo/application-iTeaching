package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteDTO {
    private Long id;
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    private String email;
    private String telefono;
}
