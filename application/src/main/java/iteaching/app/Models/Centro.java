package iteaching.app.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Getter
@Setter
@Entity
@Table(name = "Center")
public class Centro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String nombre;
    @Column(name = "address")
    private String direccion;
    @Column(name = "phone")
    private String telefono;
    @OneToMany
    private List<Asignatura> asignaturas;
    
    @OneToMany
    private List<Profesor> profesores;
    
    @OneToMany
    private List<Estudiante> estudiantes;

    public Centro(String nombre, String direccion, String telefono) {
        this.nombre =nombre;
        this.direccion=direccion;
        this.telefono=telefono;
    
    }

}