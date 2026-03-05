package iteaching.app.service;

import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.CsvImportResult;
import iteaching.app.dto.UsuarioDTO;
import iteaching.app.repository.PersonaRepository;
import iteaching.app.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonaService — unit tests")
class PersonaServiceTest {

    @Mock private PersonaRepository personaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PersonaService service;

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(1L);
        persona.setUsername("jgarcia");
        persona.setNombre("Juan");
        persona.setApellido("García");
        persona.setEmail("juan@test.com");
        persona.setTelefono("123456789");
        persona.setRole(Usuarios.Role.ROLE_ESTUDIANTE);
        persona.setPuntuacion(0.0);
    }

    @Test
    void findAll_returnsList() {
        when(personaRepository.findAll()).thenReturn(List.of(persona));
        List<UsuarioDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("jgarcia", result.get(0).getUsername());
    }

    @Test
    void findById_found() {
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        UsuarioDTO dto = service.findById(1L);
        assertEquals("Juan", dto.getNombre());
    }

    @Test
    void findById_notFound_throws() {
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(999L));
    }

    @Test
    void findByUsername_found() {
        when(personaRepository.findByUsername("jgarcia")).thenReturn(Optional.of(persona));
        UsuarioDTO dto = service.findByUsername("jgarcia");
        assertEquals("García", dto.getApellido());
    }

    @Test
    void findByUsername_notFound_throws() {
        when(personaRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findByUsername("ghost"));
    }

    @Test
    void search_returnsByNombre() {
        when(personaRepository.findByNombreContainingIgnoreCase("Juan")).thenReturn(List.of(persona));
        when(personaRepository.findByApellidoContainingIgnoreCase("Juan")).thenReturn(List.of());
        List<UsuarioDTO> result = service.search("Juan");
        assertEquals(1, result.size());
    }

    @Test
    void importFromCsv_success() {
        String csv = "username;password;nombre;apellido;email;telefono;rol\n"
                   + "user1;Pass123!;Nombre;Apellido;user1@test.com;111;ESTUDIANTE\n"
                   + "user2;Pass123!;Nombre2;Apellido2;user2@test.com;222;PROFESOR\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(personaRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> {
            Persona p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CsvImportResult result = service.importFromCsv(is);
        assertEquals(2, result.getImportados().size());
        assertTrue(result.getErrores().isEmpty());
    }

    @Test
    void importFromCsv_duplicateUsername_addsError() {
        String csv = "username;password;nombre;apellido;email\n"
                   + "existing;Pass123!;Nombre;Apellido;new@test.com\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.existsByUsername("existing")).thenReturn(true);

        CsvImportResult result = service.importFromCsv(is);
        assertEquals(0, result.getImportados().size());
        assertEquals(1, result.getErrores().size());
        assertTrue(result.getErrores().get(0).contains("ya existe"));
    }

    @Test
    void importFromCsv_duplicateEmail_addsError() {
        String csv = "username;password;nombre;apellido;email\n"
                   + "newuser;Pass123!;Nombre;Apellido;existing@test.com\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(personaRepository.existsByEmail(anyString())).thenReturn(true);

        CsvImportResult result = service.importFromCsv(is);
        assertEquals(0, result.getImportados().size());
        assertTrue(result.getErrores().get(0).contains("email"));
    }

    @Test
    void importFromCsv_invalidFormat_addsError() {
        String csv = "username;password;nombre;apellido;email\n"
                   + "only;two;fields\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        CsvImportResult result = service.importFromCsv(is);
        assertEquals(0, result.getImportados().size());
        assertEquals(1, result.getErrores().size());
        assertTrue(result.getErrores().get(0).contains("formato invalido"));
    }

    @Test
    void importFromCsv_invalidRole_addsError() {
        String csv = "username;password;nombre;apellido;email;tel;INVALIDO\n"
                   + "user1;Pass123!;N;A;e@t.com;111;INVALIDO\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(personaRepository.existsByEmail(anyString())).thenReturn(false);

        CsvImportResult result = service.importFromCsv(is);
        assertEquals(0, result.getImportados().size());
        assertTrue(result.getErrores().get(0).contains("rol invalido"));
    }

    @Test
    void importFromCsv_exceedsMaxLines_addsError() {
        StringBuilder sb = new StringBuilder("username;password;nombre;apellido;email\n");
        for (int i = 0; i < 501; i++) {
            sb.append("user").append(i).append(";pass;N;A;user").append(i).append("@t.com\n");
        }
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(personaRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> {
            Persona p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CsvImportResult result = service.importFromCsv(is);
        // First 499 data lines processed (lineNum 2..500), then lineNum 501 > 500 triggers break
        assertTrue(result.getErrores().stream().anyMatch(e -> e.contains("límite")));
        // Some were imported before the limit was hit
        assertFalse(result.getImportados().isEmpty());
    }

    @Test
    void importFromCsv_emptyRequiredFields_addsError() {
        String csv = "username;password;nombre;apellido;email\n"
                   + ";Pass123!;Nombre;Apellido;e@t.com\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        CsvImportResult result = service.importFromCsv(is);
        assertTrue(result.getErrores().get(0).contains("campos obligatorios vacios"));
    }
}
