package app.servlets;

import app.controllers.UserController;
import app.controllers.AuthController;
import app.models.User;
import app.services.EmailService;
import app.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet("/auth/resend-token")
public class ResendTokenServlet extends HttpServlet {
    private UserController userController = new UserController();
    private AuthController authController = new AuthController(); // Instancia agregada
    private EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String ip = request.getRemoteAddr();
        User user = userController.findUser(email);

        if (user != null && "Inactive".equalsIgnoreCase(user.getState())) {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            String tokenParaEnviar;

            if (ahora.after(user.getTokenExpiration())) {
                // Generar nuevo si expiró
                tokenParaEnviar = SecurityUtils.generateNineDigitToken();
                user.setToken(tokenParaEnviar);
                user.setTokenExpiration(new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000)));
                userController.updateUser(user);
                
                // AUDITORÍA: Nuevo token generado
                authController.logActivity(user.getIdUsers(), "TOKEN_REGENERATED", ip, "Token expirado, se generó uno nuevo.");
            } else {
                // Reenviar el mismo si es válido
                tokenParaEnviar = user.getToken();
                
                // AUDITORÍA: Reenvío simple
                authController.logActivity(user.getIdUsers(), "TOKEN_RESENT", ip, "Se reenvió el token original aún vigente.");
            }
            
            emailService.sendToken(email, tokenParaEnviar);
            response.sendRedirect(request.getContextPath() + "/verify.jsp?email=" + email + "&info=resent");
        } else {
            // Intento de reenvío para usuario inexistente o activo
            authController.logActivity(0, "INVALID_RESEND_ATTEMPT", ip, "Intento de reenvío para correo: " + email);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid_request");
        }
    }
}