package iteaching.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RubricaDTO {
    private Long id;
    @NotBlank
    @Size(max = 255)
    private String nombre;
    @Size(max = 2000)
    private String descripcion;
    private Long tareaId;
    private String tareaTitulo;
    private List<CriterioRubricaDTO> criterios = new ArrayList<>();
}
