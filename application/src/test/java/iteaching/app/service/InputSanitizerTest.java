package iteaching.app.service;

import iteaching.app.security.InputSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputSanitizer — XSS / HTML sanitisation")
class InputSanitizerTest {

    // ── sanitize() ──────────────────────────────────────────────

    @Test
    void sanitize_null_returnsNull() {
        assertNull(InputSanitizer.sanitize(null));
    }

    @Test
    void sanitize_plainText_unchanged() {
        assertEquals("Hola mundo", InputSanitizer.sanitize("Hola mundo"));
    }

    @Test
    void sanitize_removesScriptTags() {
        String input = "Hola <script>alert('xss')</script> mundo";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.contains("<script"));
        assertFalse(result.contains("</script>"));
    }

    @Test
    void sanitize_removesHtmlTags() {
        String input = "Texto <b>en negrita</b> y <i>cursiva</i>";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.contains("<b>"));
        assertFalse(result.contains("<i>"));
    }

    @Test
    void sanitize_removesEventHandlers() {
        String input = "onclick=alert(1) Hola";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("onclick"));
    }

    @Test
    void sanitize_removesJavascriptUri() {
        String input = "javascript:alert(1)";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    void sanitize_removesDataUri() {
        String input = "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("data:"));
    }

    @Test
    void sanitize_removesVbscriptUri() {
        String input = "vbscript:MsgBox";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("vbscript:"));
    }

    @Test
    void sanitize_removesExpressionCss() {
        String input = "expression(alert(1))";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("expression("));
    }

    @Test
    void sanitize_escapesHtmlEntities() {
        // Use isolated characters that won't form an HTML-tag pattern (<[^>]+>)
        String result = InputSanitizer.sanitize("a & b \"e\" 'f'");
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&quot;"));
        assertTrue(result.contains("&#x27;"));

        // Lone < (no matching >) is not matched by the tag regex, so it gets escaped
        String result2 = InputSanitizer.sanitize("x < y");
        assertTrue(result2.contains("&lt;"));

        // Lone > (no preceding <) is not matched by the tag regex, so it gets escaped
        String result3 = InputSanitizer.sanitize("x > y");
        assertTrue(result3.contains("&gt;"));
    }

    @Test
    void sanitize_trims() {
        assertEquals("hola", InputSanitizer.sanitize("  hola  "));
    }

    // ── sanitizeUrl() ───────────────────────────────────────────

    @Test
    void sanitizeUrl_null_returnsEmpty() {
        assertEquals("", InputSanitizer.sanitizeUrl(null));
    }

    @Test
    void sanitizeUrl_empty_returnsEmpty() {
        assertEquals("", InputSanitizer.sanitizeUrl(""));
        assertEquals("", InputSanitizer.sanitizeUrl("   "));
    }

    @Test
    void sanitizeUrl_httpAllowed() {
        assertEquals("http://example.com", InputSanitizer.sanitizeUrl("http://example.com"));
    }

    @Test
    void sanitizeUrl_httpsAllowed() {
        assertEquals("https://example.com", InputSanitizer.sanitizeUrl("https://example.com"));
    }

    @Test
    void sanitizeUrl_relativeAllowed() {
        assertEquals("/recursos/file.pdf", InputSanitizer.sanitizeUrl("/recursos/file.pdf"));
    }

    @Test
    void sanitizeUrl_javascriptBlocked() {
        assertEquals("", InputSanitizer.sanitizeUrl("javascript:alert(1)"));
    }

    @Test
    void sanitizeUrl_dataBlocked() {
        assertEquals("", InputSanitizer.sanitizeUrl("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    void sanitizeUrl_vbscriptBlocked() {
        assertEquals("", InputSanitizer.sanitizeUrl("vbscript:MsgBox"));
    }

    @Test
    void sanitizeUrl_ftpBlocked() {
        assertEquals("", InputSanitizer.sanitizeUrl("ftp://server.com/file"));
    }
}
