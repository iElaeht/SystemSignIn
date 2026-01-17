package app.services;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final Dotenv dotenv = Dotenv.load();

    public void sendToken(String recipient, String token) {
        final String sender = dotenv.get("EMAIL_USER");
        final String pass = dotenv.get("EMAIL_PASS");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, pass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender, "SystemSignIn Security"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(token + " es tu código de verificación");

            // Diseño HTML del correo
            String htmlContent = 
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                    "<div style='background-color: #2c3e50; color: white; padding: 20px; text-align: center;'>" +
                        "<h1 style='margin: 0; font-size: 24px;'>Verificación de Seguridad</h1>" +
                    "</div>" +
                    "<div style='padding: 30px; color: #333; line-height: 1.6;'>" +
                        "<p>Hola,</p>" +
                        "<p>Has solicitado un código de acceso para <strong>SystemSignIn</strong>. Utiliza el siguiente código de 9 dígitos para completar tu proceso:</p>" +
                        "<div style='background-color: #f8f9fa; border: 1px dashed #2c3e50; border-radius: 4px; padding: 20px; text-align: center; margin: 25px 0;'>" +
                            "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2c3e50;'>" + token + "</span>" +
                        "</div>" +
                        "<p style='font-size: 13px; color: #666;'>Este código es de un solo uso y <strong>expira en 10 minutos</strong>. Si no solicitaste este código, puedes ignorar este correo de forma segura.</p>" +
                    "</div>" +
                    "<div style='background-color: #f4f4f4; color: #888; padding: 15px; text-align: center; font-size: 12px;'>" +
                        "&copy; 2026 SystemSignIn Project. Todos los derechos reservados." +
                    "</div>" +
                "</div>";

            // IMPORTANTE: Definir el contenido como HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            System.out.println("DEBUG: Correo HTML enviado correctamente.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}