package iteaching.app.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import iteaching.Repositorys.UsuarioRepository;
import iteaching.app.Models.Usuarios;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void agregarUsuario(String username, String password) {
        Usuarios usuario = new Usuarios();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password)); // Se encripta la contraseña antes de guardarla en la base de datos
        userRepository.save(usuario);
    }
}
