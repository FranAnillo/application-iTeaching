package iteaching.app.config;

import iteaching.app.Models.Logro;
import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.repository.LogroRepository;
import iteaching.app.repository.UsuarioRepository;
import iteaching.app.repository.GradoRepository;
import iteaching.app.Models.Grado;
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
    private final LogroRepository logroRepository;
    private final GradoRepository gradoRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      LogroRepository logroRepository, GradoRepository gradoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.logroRepository = logroRepository;
        this.gradoRepository = gradoRepository;
    }

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
        createProfesorIfNotExists();
        createEstudianteIfNotExists();
        seedGrados();
        seedLogros();
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

    private void seedGrados() {
        if (gradoRepository.count() == 0) {
            crearGrado("Ingenieria");
            crearGrado("Matematicas");
            crearGrado("Historia");
            log.info("Grados iniciales creados");
        }
    }

    private void crearGrado(String nombre) {
        Grado g = new Grado();
        g.setNombre(nombre);
        gradoRepository.save(g);
    }

    private void seedLogros() {
        if (logroRepository.count() == 0) {
            crearLogro("PRIMERA_ENTREGA", "Primera Entrega", "Realizaste tu primera entrega de tarea", "📝", "ACADEMICO", 1);
            crearLogro("CINCO_ENTREGAS", "Estudiante Aplicado", "Completaste 5 entregas", "📚", "ACADEMICO", 5);
            crearLogro("DIEZ_ENTREGAS", "Experto en Entregas", "Completaste 10 entregas", "🎯", "ACADEMICO", 10);
            crearLogro("NOTA_PERFECTA", "Perfección", "Obtuviste la máxima calificación en una tarea", "⭐", "ACADEMICO", 1);
            crearLogro("PRIMER_FORO", "Participativo", "Creaste tu primera respuesta en el foro", "💬", "SOCIAL", 1);
            crearLogro("CINCO_FOROS", "Comunicador", "Participaste en 5 discusiones del foro", "🗣️", "SOCIAL", 5);
            crearLogro("ASISTENCIA_PERFECTA", "Asistencia Perfecta", "100% de asistencia en un mes", "🏅", "ASISTENCIA", 1);
            crearLogro("PRIMER_CURSO", "Bienvenido", "Te inscribiste en tu primer curso", "🎓", "ESPECIAL", 1);
            crearLogro("TRES_CURSOS", "Multidisciplinar", "Estás inscrito en 3 cursos", "🌟", "ESPECIAL", 3);
            crearLogro("VALORACION", "Crítico Constructivo", "Realizaste tu primera valoración", "⭐", "SOCIAL", 1);
            log.info("Logros iniciales creados");
        }
    }

    private void crearLogro(String codigo, String nombre, String descripcion, String icono, String categoria, int valorObjetivo) {
        Logro logro = new Logro();
        logro.setCodigo(codigo);
        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        logro.setIcono(icono);
        logro.setCategoria(Logro.CategoriaLogro.valueOf(categoria));
        logro.setValorObjetivo(valorObjetivo);
        logroRepository.save(logro);
    }
}
