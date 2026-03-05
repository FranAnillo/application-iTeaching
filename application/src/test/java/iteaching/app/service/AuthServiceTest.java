package iteaching.app.service;

import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AuthResponse;
import iteaching.app.dto.LoginRequest;
import iteaching.app.dto.RegisterRequest;
import iteaching.app.repository.UsuarioRepository;
import iteaching.app.security.JwtUtil;
import iteaching.app.security.PasswordPolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — unit tests")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordPolicyValidator passwordPolicyValidator;
    @Mock private AuditService auditService;

    @InjectMocks private AuthService service;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Str0ng!Pass");
        registerRequest.setNombre("Nuevo");
        registerRequest.setApellido("Usuario");
        registerRequest.setEmail("nuevo@test.com");
        registerRequest.setTelefono("123456789");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("existinguser");
        loginRequest.setPassword("Str0ng!Pass");
    }

    @Test
    void register_success() {
        when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordPolicyValidator.validate("Str0ng!Pass", "newuser"))
                .thenReturn(Collections.emptyList());
        when(passwordEncoder.encode("Str0ng!Pass")).thenReturn("encoded");
        when(usuarioRepository.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDetails ud = new User("newuser", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE")));
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ud);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(ud)).thenReturn("jwt-token");

        AuthResponse response = service.register(registerRequest);
        assertEquals("jwt-token", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals("ROLE_ESTUDIANTE", response.getRole());
        verify(auditService).logSuccess(eq("newuser"), eq("REGISTER"), anyString(), isNull());
    }

    @Test
    void register_usernameExists_throws() {
        when(usuarioRepository.existsByUsername("newuser")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.register(registerRequest));
    }

    @Test
    void register_weakPassword_throws() {
        when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordPolicyValidator.validate("Str0ng!Pass", "newuser"))
                .thenReturn(List.of("La contraseña debe contener al menos un dígito"));
        assertThrows(IllegalArgumentException.class, () -> service.register(registerRequest));
    }

    @Test
    void login_success() {
        when(auditService.countRecentFailedLogins("existinguser", 15)).thenReturn(0L);

        UserDetails ud = new User("existinguser", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_PROFESOR")));
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ud);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(ud)).thenReturn("login-token");

        AuthResponse response = service.login(loginRequest);
        assertEquals("login-token", response.getToken());
        assertEquals("existinguser", response.getUsername());
        assertEquals("ROLE_PROFESOR", response.getRole());
        verify(auditService).logSuccess(eq("existinguser"), eq("LOGIN"), anyString(), isNull());
    }

    @Test
    void login_accountLocked_throws() {
        when(auditService.countRecentFailedLogins("existinguser", 15)).thenReturn(5L);
        assertThrows(RuntimeException.class, () -> service.login(loginRequest));
        verify(auditService).logFailure(eq("existinguser"), eq("LOGIN"), anyString(), isNull());
    }

    @Test
    void login_badCredentials_throws() {
        when(auditService.countRecentFailedLogins("existinguser", 15)).thenReturn(0L);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad creds"));

        assertThrows(BadCredentialsException.class, () -> service.login(loginRequest));
        verify(auditService).logFailure(eq("existinguser"), eq("LOGIN"), anyString(), isNull());
    }
}
