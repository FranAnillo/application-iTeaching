package iteaching.app.enums;

public enum CursoAcademico {
    _2025_2026("2025-2026"),
    _2026_2027("2026-2027"),
    _2027_2028("2027-2028");

    private final String display;
    CursoAcademico(String display) { this.display = display; }
    public String getDisplay() { return display; }
}
