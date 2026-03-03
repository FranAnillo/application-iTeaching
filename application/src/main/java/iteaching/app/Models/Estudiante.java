package iteaching.app.Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estudiantes")
public class Estudiante extends Persona {

    @Column(nullable = true, length = 64)
    private String avatar;

    @JsonIgnore
    @ManyToMany(mappedBy = "estudiantes")
    private Set<Asignatura> asignaturas = new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "alumno")
    private Set<Valoracion> valoraciones = new HashSet<>();

    @Transient
    public String getAvatarImagePath() {
        if (avatar == null || getId() == null) return null;
        return "/uploads/avatars/" + avatar;
    }
}
