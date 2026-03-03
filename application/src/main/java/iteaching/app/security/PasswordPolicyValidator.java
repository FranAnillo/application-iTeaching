package iteaching.app.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates passwords according to Universidad de Sevilla security policies
 * (ENS — Esquema Nacional de Seguridad) and the US Política de Contraseñas.
 *
 * Requirements:
 *  - Minimum 8 characters
 *  - At least one uppercase letter
 *  - At least one lowercase letter
 *  - At least one digit
 *  - At least one special character (!@#$%^&*()_+-=[]{}|;:',.<>?/~`)
 *  - Must not contain the username
 */
@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;

    public List<String> validate(String password, String username) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
            return errors; // no point checking further
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("La contraseña debe contener al menos una letra minúscula");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("La contraseña debe contener al menos un dígito");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:',.<>?/~`].*")) {
            errors.add("La contraseña debe contener al menos un carácter especial");
        }
        if (username != null && !username.isEmpty()
                && password.toLowerCase().contains(username.toLowerCase())) {
            errors.add("La contraseña no puede contener el nombre de usuario");
        }

        return errors;
    }
}
