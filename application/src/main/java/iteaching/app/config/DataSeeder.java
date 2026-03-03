package iteaching.app.config;

import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
        createProfesorIfNotExists();
        createEstudianteIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Persona admin = new Persona();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin1234"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@iteaching2.com");
            admin.setTelefono("000000000");
            admin.setEnabled(true);
            admin.setRole(Usuarios.Role.ROLE_ADMIN);
            usuarioRepository.save(admin);
            log.info("Cuenta de administrador creada: admin / Admin1234");
        } else {
            log.info("Cuenta de administrador ya existe");
        }
    }

    private void createEstudianteIfNotExists() {
        if (usuarioRepository.findByUsername("estudiante").isEmpty()) {
            Persona estudiante = new Persona();
            estudiante.setUsername("estudiante");
            estudiante.setPassword(passwordEncoder.encode("Estud123!"));
            estudiante.setNombre("Estudiante");
            estudiante.setApellido("Demo");
            estudiante.setEmail("estudiante@iteaching2.com");
            estudiante.setTelefono("000000001");
            estudiante.setEnabled(true);
            estudiante.setRole(Usuarios.Role.ROLE_ESTUDIANTE);
            usuarioRepository.save(estudiante);
            log.info("Cuenta de estudiante creada: estudiante / Estud123!");
        } else {
            log.info("Cuenta de estudiante ya existe");
        }
    }

    private void createProfesorIfNotExists() {
        if (usuarioRepository.findByUsername("profesor").isEmpty()) {
            Persona profesor = new Persona();
            profesor.setUsername("profesor");
            profesor.setPassword(passwordEncoder.encode("Profe123!"));
            profesor.setNombre("Profesor");
            profesor.setApellido("Demo");
            profesor.setEmail("profesor@iteaching2.com");
            profesor.setTelefono("000000002");
            profesor.setEnabled(true);
            profesor.setRole(Usuarios.Role.ROLE_PROFESOR);
            usuarioRepository.save(profesor);
            log.info("Cuenta de profesor creada: profesor / Profe123!");
        } else {
            log.info("Cuenta de profesor ya existe");
        }
    }
}
