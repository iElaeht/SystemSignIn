package app.servlets;

import app.controllers.UserController;
import app.models.User;
import app.services.EmailService;
import app.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet("/auth/register")
public class RegistroServlet extends HttpServlet {
    private UserController userController = new UserController();
    private EmailService emailService = new EmailService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 1. Validar si el usuario ya existe
        User existente = userController.findUser(email);
        if (existente != null) {
            if (existente.getState().equalsIgnoreCase("Inactive")) {
                Timestamp ahora = new Timestamp(System.currentTimeMillis());
                // Si el token expiró (pasaron los 10 min), borramos el viejo para permitir el nuevo registro
                if (ahora.after(existente.getTokenExpiration())) {
                    userController.deleteUser(existente.getUuidUser());
                } else {
                    // Si aún es válido, solo reenviamos el correo y avisamos
                    emailService.sendToken(email, existente.getToken());
                    response.sendRedirect(request.getContextPath() + "/verify.jsp?email=" + email + "&info=resent");
                    return;
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/register.jsp?error=exists");
                return;
            }
        }

        // 2. Crear nuevo usuario con seguridad máxima
        User newUser = new User();
        newUser.setUuidUser(SecurityUtils.generateUUID());
        newUser.setUsernames(username);
        newUser.setPasswords(SecurityUtils.hashPassword(password)); // BCrypt
        newUser.setEmails(email);
        newUser.setIp(request.getRemoteAddr());
        
        String token = SecurityUtils.generateNineDigitToken();
        newUser.setToken(token);
        // Expiración: 10 minutos desde ahora
        newUser.setTokenExpiration(new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000)));
        newUser.setState("Inactive");

        if (userController.registerUser(newUser)) {
            emailService.sendToken(email, token);
            response.sendRedirect(request.getContextPath() + "/verify.jsp?email=" + email);
        } else {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=server");
        }
    }
}