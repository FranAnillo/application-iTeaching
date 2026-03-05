package iteaching.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Safe messages that can be exposed to clients (contain no internal details)
    private static final Set<String> SAFE_MESSAGE_PREFIXES = Set.of(
        "Asignatura no encontrada", "Usuario no encontrado", "Persona no encontrada",
        "Profesor no encontrado", "Estudiante no encontrado", "Tarea no encontrada",
        "Material no encontrado", "Anuncio no encontrado", "Grupo no encontrado",
        "Carpeta no encontrada", "Clase no encontrada", "Tema no encontrado",
        "Respuesta no encontrada", "Rúbrica no encontrada", "Notificación no encontrada",
        "Logro no encontrado", "Valoración no encontrada", "Entrega no encontrada",
        "El usuario no tiene rol", "Solo los estudiantes pueden",
        "Ya has valorado", "El profesor seleccionado no imparte",
        "Solo los estudiantes matriculados", "El profesor no ha habilitado",
        "El CSV excede el límite", "Error al procesar el CSV",
        "el usuario", "el email", "campos obligatorios", "formato invalido",
        "rol invalido", "Acceso denegado", "Error de validación",
        "Puntos de mejora:", "El contenido no es apropiado"
    );

    private String sanitizeMessage(String message) {
        if (message == null) return "Error interno del servidor";
        for (String prefix : SAFE_MESSAGE_PREFIXES) {
            if (message.startsWith(prefix) || message.contains(prefix)) {
                return message;
            }
        }
        // If the message doesn't match known safe patterns, hide internal details
        return "Error interno del servidor";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", "Acceso denegado: no tiene permisos suficientes");
        body.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", sanitizeMessage(ex.getMessage()));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", sanitizeMessage(ex.getMessage()));
        body.put("status", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", "Usuario o contraseña incorrectos");
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", "Error de validación");
        body.put("errors", errors);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", "Error interno del servidor");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
