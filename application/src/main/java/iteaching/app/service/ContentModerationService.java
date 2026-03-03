package iteaching.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio de moderación de contenido basado en IA heurística.
 * Detecta y bloquea lenguaje ofensivo, insultos y expresiones malsonantes
 * en las valoraciones de profesores.
 *
 * Utiliza múltiples capas de análisis:
 * 1. Diccionario de palabras prohibidas (español e inglés)
 * 2. Detección de variaciones con caracteres sustituidos (l33t speak)
 * 3. Análisis de patrones ofensivos
 * 4. Detección de ataques personales
 */
@Service
public class ContentModerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentModerationService.class);

    // ── Palabras y expresiones prohibidas (insultos en español) ──
    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList(
        // Insultos directos
        "idiota", "imbecil", "imbécil", "estupido", "estúpido", "subnormal",
        "retrasado", "tonto", "gilipollas", "capullo", "cabrón", "cabron",
        "hijo de puta", "hijoputa", "hijodeputa", "hdp", "puta", "puto",
        "mierda", "mrd", "basura humana", "inutil", "inútil", "payaso",
        "incompetente total", "mediocre", "zoquete", "palurdo", "cretino",
        "necio", "bobo", "tarado", "mongol", "memo",
        // Vulgaridades
        "joder", "coño", "cojones", "pollas", "hostia", "hostias",
        "carajo", "verga", "pendejo", "culero", "chingar", "chingada",
        "pinche", "cabronada", "maricón", "maricon", "marica",
        // Amenazas e intimidación
        "te voy a", "voy a darte", "te mereces que te",
        "ojalá te", "ojala te", "que te jodan", "vete a la mierda",
        "que te den", "asco de persona", "asco de profesor",
        // Discriminación
        "machista", "fascista", "nazi",
        // Insultos en inglés comunes
        "fuck", "shit", "asshole", "bitch", "bastard", "damn",
        "dumbass", "moron", "loser", "trash", "piece of shit",
        "stupid", "idiot", "retard", "crap"
    );

    // ── Patrones ofensivos (regex) ──
    private static final List<Pattern> PATRONES_OFENSIVOS = Arrays.asList(
        // Ataques personales directos
        Pattern.compile("\\b(eres|es)\\s+un[ao]?\\s+(mierda|basura|asco|desastre|vergüenza|verguenza|inutil|inútil|desgracia)\\b", Pattern.CASE_INSENSITIVE),
        // "No sirves para nada", "no vale para nada"
        Pattern.compile("\\bno\\s+(sirve|vales?|sabe|entiende)\\s+(para\\s+)?nada\\b", Pattern.CASE_INSENSITIVE),
        // Deseos negativos
        Pattern.compile("\\b(ojalá|ojala|espero que)\\s+(te|le|se)\\s+(muera|despidan|echen|largue)\\b", Pattern.CASE_INSENSITIVE),
        // Insultos con variaciones de espacios
        Pattern.compile("\\bh+i+j+o+\\s*d+e+\\s*p+", Pattern.CASE_INSENSITIVE),
        // Repetición excesiva de caracteres (frustración agresiva): "noooooo", "maloooo"
        Pattern.compile("(.)\\1{4,}"),
        // Uso excesivo de mayúsculas (gritar) - más de 60% mayúsculas en texto largo
        Pattern.compile("^[^a-z]*$") // se evalúa aparte para textos >20 chars
    );

    // ── Sustituciones de leet speak ──
    private static final String[][] LEET_SUSTITUCIONES = {
        {"0", "o"}, {"1", "i"}, {"3", "e"}, {"4", "a"}, {"5", "s"},
        {"7", "t"}, {"@", "a"}, {"!", "i"}, {"$", "s"}, {"|", "l"},
        {"\\*", ""}, {"\\.", ""}, {"-", ""}, {"_", ""}
    };

    /**
     * Resultado del análisis de moderación.
     */
    public static class ModerationResult {
        private final boolean approved;
        private final String reason;

        public ModerationResult(boolean approved, String reason) {
            this.approved = approved;
            this.reason = reason;
        }

        public boolean isApproved() { return approved; }
        public String getReason() { return reason; }
    }

    /**
     * Analiza un texto y determina si es apropiado para publicar.
     * @param texto el comentario a analizar
     * @return ModerationResult con el resultado y motivo si es rechazado
     */
    public ModerationResult moderate(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ModerationResult(true, null);
        }

        String original = texto.trim();

        // 1. Verificar longitud mínima útil
        if (original.length() < 3) {
            return new ModerationResult(true, null);
        }

        // 2. Detectar uso excesivo de mayúsculas (gritar)
        if (original.length() > 20) {
            long upperCount = original.chars().filter(Character::isUpperCase).count();
            long letterCount = original.chars().filter(Character::isLetter).count();
            if (letterCount > 0 && (double) upperCount / letterCount > 0.6) {
                log.warn("Contenido rechazado por uso excesivo de mayúsculas: {}", truncate(original));
                return new ModerationResult(false,
                    "El comentario contiene demasiadas mayúsculas. Por favor, escribe de forma normal.");
            }
        }

        // 3. Normalizar texto para detección
        String normalizado = normalizar(original);
        String sinLeet = deshacerLeet(normalizado);

        // 4. Buscar palabras prohibidas en texto normalizado y versión sin leet
        for (String palabra : PALABRAS_PROHIBIDAS) {
            String palabraNorm = normalizar(palabra);
            if (contieneFlexible(normalizado, palabraNorm) || contieneFlexible(sinLeet, palabraNorm)) {
                log.warn("Contenido rechazado por palabra prohibida '{}': {}", palabra, truncate(original));
                return new ModerationResult(false,
                    "El comentario contiene lenguaje inapropiado. Las valoraciones deben ser respetuosas y constructivas.");
            }
        }

        // 5. Evaluar patrones ofensivos
        for (Pattern patron : PATRONES_OFENSIVOS) {
            if (patron.matcher(original).find() || patron.matcher(normalizado).find()) {
                log.warn("Contenido rechazado por patrón ofensivo: {}", truncate(original));
                return new ModerationResult(false,
                    "El comentario contiene expresiones inapropiadas. Por favor, reformula tu valoración de forma constructiva.");
            }
        }

        // 6. Detectar repetición excesiva de signos de puntuación (!!!! ????)
        if (Pattern.compile("[!?]{3,}").matcher(original).find()) {
            log.warn("Contenido rechazado por puntuación excesiva: {}", truncate(original));
            return new ModerationResult(false,
                "Por favor, evita el uso excesivo de signos de exclamación o interrogación.");
        }

        log.debug("Contenido aprobado por moderación: {}", truncate(original));
        return new ModerationResult(true, null);
    }

    /**
     * Normaliza texto: elimina acentos, convierte a minúsculas, simplifica espacios.
     */
    private String normalizar(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        normalizado = normalizado.toLowerCase();
        normalizado = normalizado.replaceAll("\\s+", " ").trim();
        return normalizado;
    }

    /**
     * Deshace sustituciones de leet speak (1d10t4 → idiota)
     */
    private String deshacerLeet(String texto) {
        String result = texto;
        for (String[] sub : LEET_SUSTITUCIONES) {
            result = result.replaceAll(sub[0], sub[1]);
        }
        return result;
    }

    /**
     * Búsqueda flexible: detecta la palabra con posibles caracteres separadores.
     */
    private boolean contieneFlexible(String texto, String palabra) {
        // Búsqueda directa con límites de palabra
        if (Pattern.compile("\\b" + Pattern.quote(palabra) + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(texto).find()) {
            return true;
        }

        // Búsqueda sin espacios (para detectar "h i j o d e p u t a")
        String textoSinEspacios = texto.replaceAll("\\s+", "");
        String palabraSinEspacios = palabra.replaceAll("\\s+", "");
        if (textoSinEspacios.contains(palabraSinEspacios)) {
            return true;
        }

        return false;
    }

    /**
     * Trunca texto para logging seguro.
     */
    private String truncate(String text) {
        if (text.length() <= 50) return text;
        return text.substring(0, 50) + "...";
    }
}
