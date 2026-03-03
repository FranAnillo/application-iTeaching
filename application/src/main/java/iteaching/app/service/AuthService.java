package iteaching.app.service;

import iteaching.app.Models.Persona;
import iteaching.app.Models.Usuarios;
import iteaching.app.dto.AuthResponse;
import iteaching.app.dto.LoginRequest;
import iteaching.app.dto.RegisterRequest;
import iteaching.app.repository.UsuarioRepository;
import iteaching.app.security.JwtUtil;
import iteaching.app.security.PasswordPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_WINDOW_MINUTES = 15;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final AuditService auditService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                       PasswordPolicyValidator passwordPolicyValidator, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Validate password against US security policy
        List<String> pwErrors = passwordPolicyValidator.validate(
                request.getPassword(), request.getUsername());
        if (!pwErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(". ", pwErrors));
        }

        Persona persona = new Persona();
        persona.setUsername(request.getUsername());
        persona.setPassword(passwordEncoder.encode(request.getPassword()));
        persona.setNombre(request.getNombre());
        persona.setApellido(request.getApellido());
        persona.setEmail(request.getEmail());
        persona.setTelefono(request.getTelefono());
        persona.setEnabled(true);
        persona.setRole(Usuarios.Role.ROLE_ESTUDIANTE);

        usuarioRepository.save(persona);

        auditService.logSuccess(request.getUsername(), "REGISTER",
                "Nuevo usuario registrado", null);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtUtil.generateToken((UserDetails) auth.getPrincipal());
        return new AuthResponse(token, request.getUsername(), Usuarios.Role.ROLE_ESTUDIANTE.name());
    }

    public AuthResponse login(LoginRequest request) {
        // Check account lockout
        long recentFailures = auditService.countRecentFailedLogins(
                request.getUsername(), LOCKOUT_WINDOW_MINUTES);
        if (recentFailures >= MAX_FAILED_ATTEMPTS) {
            auditService.logFailure(request.getUsername(), "LOGIN",
                    "Cuenta bloqueada temporalmente por exceso de intentos fallidos", null);
            throw new RuntimeException(
                    "Cuenta bloqueada temporalmente. Demasiados intentos fallidos. "
                    + "Inténtelo de nuevo en " + LOCKOUT_WINDOW_MINUTES + " minutos.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("ROLE_ESTUDIANTE");

            auditService.logSuccess(request.getUsername(), "LOGIN", "Login exitoso", null);

            return new AuthResponse(token, userDetails.getUsername(), role);
        } catch (BadCredentialsException ex) {
            auditService.logFailure(request.getUsername(), "LOGIN",
                    "Credenciales incorrectas (intento " + (recentFailures + 1) + "/" + MAX_FAILED_ATTEMPTS + ")", null);
            throw ex;
        }
    }
}
