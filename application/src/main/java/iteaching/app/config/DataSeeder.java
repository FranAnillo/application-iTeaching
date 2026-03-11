package iteaching.app.config;

import iteaching.app.Models.*;
import iteaching.app.enums.CursoAcademico;
import iteaching.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogroRepository logroRepository;
    private final GradoRepository gradoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final AnuncioRepository anuncioRepository;
    private final MaterialRepository materialRepository;
    private final CarpetaRepository carpetaRepository;
    private final TareaRepository tareaRepository;
    private final EntregaRepository entregaRepository;
    private final ForoTemaRepository foroTemaRepository;
    private final ForoRespuestaRepository foroRespuestaRepository;
    private final GrupoRepository grupoRepository;
    private final ClaseRepository claseRepository;

    public DataSeeder(PersonaRepository personaRepository, PasswordEncoder passwordEncoder,
                      LogroRepository logroRepository, GradoRepository gradoRepository,
                      AsignaturaRepository asignaturaRepository, AnuncioRepository anuncioRepository,
                      MaterialRepository materialRepository, CarpetaRepository carpetaRepository,
                      TareaRepository tareaRepository, EntregaRepository entregaRepository,
                      ForoTemaRepository foroTemaRepository, ForoRespuestaRepository foroRespuestaRepository,
                      GrupoRepository grupoRepository, ClaseRepository claseRepository) {
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.logroRepository = logroRepository;
        this.gradoRepository = gradoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.anuncioRepository = anuncioRepository;
        this.materialRepository = materialRepository;
        this.carpetaRepository = carpetaRepository;
        this.tareaRepository = tareaRepository;
        this.entregaRepository = entregaRepository;
        this.foroTemaRepository = foroTemaRepository;
        this.foroRespuestaRepository = foroRespuestaRepository;
        this.grupoRepository = grupoRepository;
        this.claseRepository = claseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Persona admin = createAdminIfNotExists();
        Persona profesor = createProfesorIfNotExists();
        Persona estudiante = createEstudianteIfNotExists();
        
        seedLogros();
        
        if (gradoRepository.count() == 0) {
            Grado ingenieria = crearGrado("Ingeniería Informática", CursoAcademico._2025_2026);
            Grado mates = crearGrado("Matemáticas", CursoAcademico._2025_2026);
            crearGrado("Historia", CursoAcademico._2025_2026);

            if (asignaturaRepository.count() == 0) {
                Asignatura asig1 = crearAsignatura("Programación Avanzada", "PROG", "Curso sobre Java, Spring Boot y patrones de diseño.", ingenieria, profesor, estudiante, admin);
                crearAsignatura("Arquitectura de Software", "ARQ", "Diseño de sistemas escalables y microservicios.", ingenieria, profesor, estudiante, admin);
                crearAsignatura("Cálculo I", "CAL1", "Límites, derivadas e integrales.", mates, profesor, estudiante, admin);

                seedAsignaturaContent(asig1, profesor, estudiante);
            }
        }
    }

    private Persona createAdminIfNotExists() {
        return personaRepository.findByUsername("admin").orElseGet(() -> {
            Persona admin = new Persona();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin1234"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@iteaching2.com");
            admin.setTelefono("000000000");
            admin.setEnabled(true);
            admin.setRole(Usuarios.Role.ROLE_ADMIN);
            log.info("Cuenta de administrador creada: admin / Admin1234");
            return personaRepository.save(admin);
        });
    }

    private Persona createEstudianteIfNotExists() {
        return personaRepository.findByUsername("estudiante").orElseGet(() -> {
            Persona estudiante = new Persona();
            estudiante.setUsername("estudiante");
            estudiante.setPassword(passwordEncoder.encode("Estud123!"));
            estudiante.setNombre("Estudiante");
            estudiante.setApellido("Demo");
            estudiante.setEmail("estudiante@iteaching2.com");
            estudiante.setTelefono("000000001");
            estudiante.setEnabled(true);
            estudiante.setRole(Usuarios.Role.ROLE_ESTUDIANTE);
            log.info("Cuenta de estudiante creada: estudiante / Estud123!");
            return personaRepository.save(estudiante);
        });
    }

    private Persona createProfesorIfNotExists() {
        return personaRepository.findByUsername("profesor").orElseGet(() -> {
            Persona profesor = new Persona();
            profesor.setUsername("profesor");
            profesor.setPassword(passwordEncoder.encode("Profe123!"));
            profesor.setNombre("Profesor");
            profesor.setApellido("Demo");
            profesor.setEmail("profesor@iteaching2.com");
            profesor.setTelefono("000000002");
            profesor.setEnabled(true);
            profesor.setRole(Usuarios.Role.ROLE_PROFESOR);
            log.info("Cuenta de profesor creada: profesor / Profe123!");
            return personaRepository.save(profesor);
        });
    }

    private Grado crearGrado(String nombre, CursoAcademico curso) {
        Grado g = new Grado();
        g.setNombre(nombre);
        g.setCursoAcademico(curso);
        return gradoRepository.save(g);
    }

    private Asignatura crearAsignatura(String nombre, String siglas, String desc, Grado grado, Persona profesor, Persona estudiante, Persona admin) {
        Asignatura a = new Asignatura();
        a.setNombre(nombre);
        a.setSiglas(siglas);
        a.setDescripcion(desc);
        a.setGrado(grado);
        a.setCreador(admin);
        a.setProfesores(new ArrayList<>(List.of(profesor)));
        a.setEstudiantes(new ArrayList<>(List.of(estudiante)));
        return asignaturaRepository.save(a);
    }

    private void seedAsignaturaContent(Asignatura asig, Persona profesor, Persona estudiante) {
        // Anuncios
        crearAnuncio("¡Bienvenidos al curso!", "Espero que disfrutéis aprendiendo Java y Spring.", true, asig, profesor);
        crearAnuncio("Próxima tutoría", "El lunes a las 10:00 habrá tutoría online.", false, asig, profesor);

        // Carpetas y Materiales
        Carpeta c1 = crearCarpeta("Semana 1: Introducción", asig);
        crearMaterial("Transparencias Tema 1", "PDF con la teoría inicial.", "http://example.com/tema1.pdf", Material.TipoMaterial.DOCUMENTO, asig, c1, profesor);
        
        Carpeta c2 = crearCarpeta("Recursos Adicionales", asig);
        crearMaterial("Repositorio GitHub", "Código fuente de los ejemplos.", "https://github.com/iteaching/demo", Material.TipoMaterial.ENLACE, asig, c2, profesor);

        // Tareas y Entregas
        Tarea t1 = crearTarea("Entrega Inicial", "Sube un documento con tu propuesta de proyecto.", LocalDateTime.now().plusDays(7), asig, profesor, Tarea.TipoTarea.TAREA);
        crearEntrega("Aquí está mi propuesta. He incluido un diagrama de clases.", null, LocalDateTime.now().plusHours(2), t1, estudiante);

        crearTarea("Examen Parcial", "Examen teórico sobre los temas 1-3.", LocalDateTime.now().plusDays(15), asig, profesor, Tarea.TipoTarea.EVALUACION);

        // Foro
        ForoTema tema = crearForoTema("Duda con la inyección de dependencias", "¿Qué diferencia hay entre @Autowired y el constructor?", asig, estudiante);
        crearForoRespuesta("Es mejor usar el constructor porque facilita los tests unitarios.", tema, profesor);

        // Grupos
        crearGrupo("Grupo de Prácticas A", Grupo.TipoGrupo.PRACTICA, true, asig);
        crearGrupo("Grupo de Teoría 1", Grupo.TipoGrupo.TEORIA, false, asig);

        // Clases / Horario (Usando String para las horas según el modelo Clase)
        crearClase("2026-03-16T09:00:00", "2026-03-16T11:00:00", asig, profesor, estudiante);
        crearClase("2026-03-18T11:30:00", "2026-03-18T13:30:00", asig, profesor, estudiante);
    }

    private void crearAnuncio(String titulo, String cont, boolean imp, Asignatura asig, Persona autor) {
        Anuncio a = new Anuncio();
        a.setTitulo(titulo);
        a.setContenido(cont);
        a.setImportante(imp);
        a.setAsignatura(asig);
        a.setAutor(autor);
        anuncioRepository.save(a);
    }

    private Carpeta crearCarpeta(String nombre, Asignatura asig) {
        Carpeta c = new Carpeta();
        c.setNombre(nombre);
        c.setAsignatura(asig);
        return carpetaRepository.save(c);
    }

    private void crearMaterial(String titulo, String desc, String url, Material.TipoMaterial tipo, Asignatura asig, Carpeta carp, Persona autor) {
        Material m = new Material();
        m.setTitulo(titulo);
        m.setDescripcion(desc);
        m.setUrlRecurso(url);
        m.setTipo(tipo);
        m.setAsignatura(asig);
        m.setCarpeta(carp);
        m.setAutor(autor);
        m.setFechaCreacion(LocalDateTime.now());
        materialRepository.save(m);
    }

    private Tarea crearTarea(String tit, String desc, LocalDateTime entrega, Asignatura asig, Persona creador, Tarea.TipoTarea tipo) {
        Tarea t = new Tarea();
        t.setTitulo(tit);
        t.setDescripcion(desc);
        t.setFechaEntrega(entrega);
        t.setAsignatura(asig);
        t.setCreador(creador);
        t.setTipoTarea(tipo);
        return tareaRepository.save(t);
    }

    private void crearEntrega(String cont, String url, LocalDateTime fecha, Tarea tarea, Persona estudiante) {
        Entrega e = new Entrega();
        e.setContenido(cont);
        e.setUrlAdjunto(url);
        e.setFechaEntrega(fecha);
        e.setTarea(tarea);
        e.setEstudiante(estudiante);
        entregaRepository.save(e);
    }

    private ForoTema crearForoTema(String tit, String cont, Asignatura asig, Persona autor) {
        ForoTema f = new ForoTema();
        f.setTitulo(tit);
        f.setContenido(cont);
        f.setAsignatura(asig);
        f.setAutor(autor);
        return foroTemaRepository.save(f);
    }

    private void crearForoRespuesta(String cont, ForoTema tema, Persona autor) {
        ForoRespuesta r = new ForoRespuesta();
        r.setContenido(cont);
        r.setTema(tema);
        r.setAutor(autor);
        foroRespuestaRepository.save(r);
    }

    private void crearGrupo(String nom, Grupo.TipoGrupo tipo, boolean inscr, Asignatura asig) {
        Grupo g = new Grupo();
        g.setNombre(nom);
        g.setTipo(tipo);
        g.setInscribible(inscr);
        g.setAsignatura(asig);
        grupoRepository.save(g);
    }

    private void crearClase(String inicio, String fin, Asignatura asig, Persona prof, Persona alum) {
        Clase c = new Clase();
        c.setHoraComienzo(inicio);
        c.setHoraFin(fin);
        c.setAsignatura(asig);
        c.setProfesor(prof);
        c.setAlumno(alum);
        c.setEstadoClase(EstadoClase.ACEPTADA);
        claseRepository.save(c);
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

