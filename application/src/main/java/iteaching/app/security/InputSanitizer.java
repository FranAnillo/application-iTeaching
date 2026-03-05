package iteaching.app.security;

import java.util.regex.Pattern;

/**
 * Utilidad para sanitizar entradas de texto contra XSS e inyección HTML.
 * Se usa en los servicios antes de persistir contenido libre de usuario.
 */
public final class InputSanitizer {

    private InputSanitizer() {}

    // Patrones peligrosos de HTML/JS
    private static final Pattern SCRIPT_TAG = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_HANDLER = Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_URI = Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_URI = Pattern.compile("data\\s*:[^,]*;base64", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPRESSION = Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_URI = Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitiza un texto eliminando etiquetas HTML, scripts y patrones peligrosos.
     * Devuelve null si la entrada es null.
     */
    public static String sanitize(String input) {
        if (input == null) return null;

        String clean = input;
        clean = SCRIPT_TAG.matcher(clean).replaceAll("");
        clean = HTML_TAG.matcher(clean).replaceAll("");
        clean = EVENT_HANDLER.matcher(clean).replaceAll("");
        clean = JAVASCRIPT_URI.matcher(clean).replaceAll("");
        clean = DATA_URI.matcher(clean).replaceAll("");
        clean = EXPRESSION.matcher(clean).replaceAll("");
        clean = VBSCRIPT_URI.matcher(clean).replaceAll("");

        // Escapar caracteres especiales HTML restantes
        clean = clean.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;");

        return clean.trim();
    }

    /**
     * Sanitiza una URL, verificando que use protocolos seguros.
     * Devuelve cadena vacía si el protocolo no es válido.
     */
    public static String sanitizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) return "";
        String trimmed = url.trim().toLowerCase();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("/")) {
            return url.trim();
        }
        // Rechazar protocolos peligrosos como javascript:, data:, vbscript:
        return "";
    }
}
