package iteaching.app.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import iteaching.app.Models.Asignatura;
import iteaching.app.Models.Clase;
import iteaching.app.Models.Persona;
import iteaching.app.repository.AsignaturaRepository;
import iteaching.app.repository.ClaseRepository;
import iteaching.app.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioPdfService {

    private final ClaseRepository claseRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PersonaRepository personaRepository;

    public HorarioPdfService(ClaseRepository claseRepository,
                             AsignaturaRepository asignaturaRepository,
                             PersonaRepository personaRepository) {
        this.claseRepository = claseRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.personaRepository = personaRepository;
    }

    /**
     * Genera un PDF con el horario de clases de un estudiante para una asignatura.
     */
    public byte[] generarHorarioPdf(Long asignaturaId, Long alumnoId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));
        Persona alumno = personaRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        List<Clase> clases = claseRepository.findByAsignaturaIdAndAlumnoId(asignaturaId, alumnoId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // ---- Fonts ----
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(30, 58, 138));
            Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(75, 85, 99));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(31, 41, 55));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(156, 163, 175));

            // ---- Title ----
            Paragraph title = new Paragraph("iTeaching - Horario de Clases", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);

            // ---- Subtitle: asignatura + student ----
            Paragraph sub = new Paragraph("Asignatura: " + asignatura.getNombre(), subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(4);
            document.add(sub);

            Paragraph studentInfo = new Paragraph("Estudiante: " + alumno.getNombreCompleto(), subtitleFont);
            studentInfo.setAlignment(Element.ALIGN_CENTER);
            studentInfo.setSpacingAfter(4);
            document.add(studentInfo);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph dateInfo = new Paragraph("Fecha de generacion: " + fecha, footerFont);
            dateInfo.setAlignment(Element.ALIGN_CENTER);
            dateInfo.setSpacingAfter(20);
            document.add(dateInfo);

            // ---- Separator line ----
            Paragraph separator = new Paragraph(" ");
            separator.setSpacingAfter(5);
            document.add(separator);

            if (clases.isEmpty()) {
                Paragraph noData = new Paragraph("No hay clases programadas para este estudiante en esta asignatura.", cellFont);
                noData.setAlignment(Element.ALIGN_CENTER);
                noData.setSpacingBefore(30);
                document.add(noData);
            } else {
                addCalendarGrid(document, clases, cellFont, headerFont);
            }

            // ---- Footer ----
            Paragraph footer = new Paragraph(
                    "\niTeaching 2.0 - Documento generado automáticamente",
                    footerFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de horario", e);
        }

        return baos.toByteArray();
    }

    private void addCalendarGrid(Document document, List<Clase> clases, Font cellFont, Font headerFont) throws DocumentException {
        // ---- Table setup (Time + 5 Days) ----
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 2f, 2f, 2f, 2f});
        table.setSpacingBefore(10);

        Color headerBg = new Color(30, 58, 138); // navy blue
        addHeaderCell(table, "Hora", headerFont, headerBg);
        addHeaderCell(table, "Lunes", headerFont, headerBg);
        addHeaderCell(table, "Martes", headerFont, headerBg);
        addHeaderCell(table, "Miércoles", headerFont, headerBg);
        addHeaderCell(table, "Jueves", headerFont, headerBg);
        addHeaderCell(table, "Viernes", headerFont, headerBg);

        // Sorting and grouping (approximate weekly view)
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        
        // Define rows from 08:00 to 20:00
        for (int h = 8; h <= 20; h++) {
            LocalTime startHour = LocalTime.of(h, 0);
            LocalTime endHour = LocalTime.of(h, 59);
            
            // Time column
            addDataCell(table, String.format("%02d:00", h), cellFont, new Color(243, 244, 246));

            for (DayOfWeek day : days) {
                // Find classes on this day that overlap with this hour
                List<Clase> dayClasses = clases.stream()
                        .filter(c -> c.getHoraComienzo().getDayOfWeek() == day)
                        .filter(c -> {
                            LocalTime startTime = c.getHoraComienzo().toLocalTime();
                            LocalTime endTime = c.getHoraFin().toLocalTime();
                            return !startTime.isAfter(endHour) && !endTime.isBefore(startHour);
                        })
                        .collect(Collectors.toList());

                if (dayClasses.isEmpty()) {
                    addDataCell(table, "", cellFont, Color.WHITE);
                } else {
                    StringBuilder cellText = new StringBuilder();
                    for (Clase c : dayClasses) {
                        cellText.append(c.getTitulo() != null ? c.getTitulo() : "Clase").append("\n");
                        cellText.append(c.getHoraComienzo().toLocalTime().toString()).append(" - ");
                        cellText.append(c.getHoraFin().toLocalTime().toString());
                        if (c.getAula() != null) cellText.append("\n(").append(c.getAula()).append(")");
                    }
                    Color cellColor = new Color(219, 234, 254); // Light blue
                    addDataCell(table, cellText.toString(), new Font(Font.HELVETICA, 8), cellColor);
                }
            }
        }

        document.add(table);
    }

    /**
     * Genera un PDF con TODAS las clases de una asignatura (para profesores/admin).
     */
    public byte[] generarHorarioCompletoPdf(Long asignaturaId) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        List<Clase> clases = claseRepository.findByAsignaturaId(asignaturaId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(30, 58, 138));
            Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(75, 85, 99));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(31, 41, 55));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(156, 163, 175));

            Paragraph title = new Paragraph("iTeaching - Horario Completo", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);

            Paragraph sub = new Paragraph("Asignatura: " + asignatura.getNombre(), subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(4);
            document.add(sub);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph dateInfo = new Paragraph("Fecha de generación: " + fecha, footerFont);
            dateInfo.setAlignment(Element.ALIGN_CENTER);
            dateInfo.setSpacingAfter(20);
            document.add(dateInfo);

            if (clases.isEmpty()) {
                Paragraph noData = new Paragraph("No hay clases programadas para esta asignatura.", cellFont);
                noData.setAlignment(Element.ALIGN_CENTER);
                noData.setSpacingBefore(30);
                document.add(noData);
            } else {
                addCalendarGrid(document, clases, cellFont, headerFont);
            }

            Paragraph footer = new Paragraph(
                    "\niTeaching 2.0 - Documento generado automáticamente",
                    footerFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de horario", e);
        }

        return baos.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setBorderWidth(0);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private Color getEstadoColor(String estado) {
        switch (estado) {
            case "ACEPTADA":
                return new Color(22, 163, 74);   // green
            case "SOLICITADA":
                return new Color(202, 138, 4);   // yellow
            case "RECHAZADA":
                return new Color(220, 38, 38);   // red
            case "COMPLETADA":
                return new Color(37, 99, 235);   // blue
            case "CANCELADA":
                return new Color(107, 114, 128);  // gray
            default:
                return new Color(75, 85, 99);
        }
    }
}
