package app.servlets;

import app.controllers.AuthController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/auth/verify")
public class VerifyTokenServlet extends HttpServlet {
    private AuthController auth = new AuthController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String token = request.getParameter("token");
        String ip = request.getRemoteAddr();

        // Intentar activar la cuenta usando la lógica de 10 minutos de la DB
        if (auth.verifyAndActivate(email, token)) {
            // Éxito: Registro en auditoría y limpieza de intentos
            auth.logActivity(0, "ACCOUNT_ACTIVATED", ip, "Correo verificado con éxito: " + email);
            
            response.sendRedirect(request.getContextPath() + "/login.jsp?success=verified");
        } else {
            // Fallo: Puede ser token incorrecto o que ya pasaron los 10 minutos
            auth.logActivity(0, "VERIFY_FAILED", ip, "Intento de token fallido o expirado para: " + email);
            response.sendRedirect(request.getContextPath() + "/verify.jsp?email=" + email + "&error=invalid");
        }
    }
}