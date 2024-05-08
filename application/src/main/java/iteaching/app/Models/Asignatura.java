package iteaching.app.Models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asignatura {
    
    String nombre;

    TipoAsignatura tipoAsignatura;

    List<Estudiante> estudiantes;


}