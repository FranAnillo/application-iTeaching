package iteaching.app.Models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Person")
@MappedSuperclass
public class Persona extends Usuarios{
    
    @NotEmpty
    @Column(name = "name")
    protected String nombre;

    @NotEmpty
    @Column(name = "surname")
    protected String apellido;

    @Column(name = "telephone")
	@NotEmpty
	@Digits(fraction = 0, integer = 10)
	private String telefono;

	@Column(name= "email")
	@NotEmpty
	@Pattern(regexp = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")
	private String email;
	public String getFirstName() {
		return this.nombre;
	}

	@Transient
	public String getFullName() {
		return nombre+" "+apellido;
	}

}
