package iteaching.app.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "personas")
public class Persona extends Usuarios {

    @NotBlank
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "telefono")
    @Digits(fraction = 0, integer = 15)
    private String telefono;

    @Column(name = "email", unique = true)
    @NotBlank
    @Pattern(regexp = "^[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,6}$")
    private String email;

    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
