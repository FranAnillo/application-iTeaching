package iteaching.app.dto;

import iteaching.app.enums.CursoAcademico;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GradoDTO {
    private Long id;
    private String nombre;
    private CursoAcademico cursoAcademico;
    private String centroImparticion;
}
