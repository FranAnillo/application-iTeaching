package iteaching.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // mailSender is only required in production; in the local profile we don't have a SMTP server,
    // so we mark the dependency as optional.  sendNotification() will be no-op when it's null.
    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendNotification(String to, String subject, String text) {
        if (mailSender == null) {
            // running without mail support (local profile); ignore
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void notifyGrade(String to, String asignatura, String tarea, String calificacion) {
        String subject = "Nueva calificación en " + asignatura;
        String text = "Has recibido una calificación de " + calificacion + " en la tarea '" + tarea + "'.";
        sendNotification(to, subject, text);
    }

    public void notifyAchievement(String to, String logro) {
        String subject = "¡Nuevo logro desbloqueado!";
        String text = "Has conseguido el logro: " + logro;
        sendNotification(to, subject, text);
    }

    public void notifyMessage(String to, String remitente, String contenido) {
        String subject = "Nuevo mensaje de " + remitente;
        String text = "Has recibido un mensaje: " + contenido;
        sendNotification(to, subject, text);
    }

    public void notifyContentAdded(String to, String asignatura, String contenido) {
        String subject = "Nuevo contenido en " + asignatura;
        String text = "Se ha añadido contenido: " + contenido;
        sendNotification(to, subject, text);
    }

    public void notifyMaterialUploaded(String to, String asignatura, String material) {
        String subject = "Nuevo material en " + asignatura;
        String text = "Se ha subido el material: " + material;
        sendNotification(to, subject, text);
    }
}
