package iteaching.app.Models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Profesor extends Persona{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


}
