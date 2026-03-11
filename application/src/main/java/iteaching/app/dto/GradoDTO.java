package iteaching.app.dto;

import iteaching.app.enums.CursoAcademico;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class GradoDTO {
    private Long id;
    private String nombre;
    private CursoAcademico cursoAcademico;
    private String centroImparticion;
    private List<Long> asignaturaIds;
}
