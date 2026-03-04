package iteaching.app.dto;

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
    private String nombre;
    private String descripcion;
    private Long tareaId;
    private String tareaTitulo;
    private List<CriterioRubricaDTO> criterios = new ArrayList<>();
}
