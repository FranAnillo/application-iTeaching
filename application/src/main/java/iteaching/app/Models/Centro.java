package iteaching.app.Models;


public class Centro {

    private String nombre;
    private String direccion;
    private String telefono;
    private List<Asignatura> asignaturas;
    private List<Profesores> profesores;
    private List<Estudiante> estudiantes;

    public CentroEstudios(String nombre, String direccion, String telefono) {
        this.nombre =nombre;
        this.direccion=direccion;
        this.telefono=telefono;
    
    }

}