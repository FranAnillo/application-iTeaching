package iteaching.app.Models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Usuarios {

    @Id
    String username;


    String password;


    boolean enable;


    
}
