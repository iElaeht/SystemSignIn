package app.servlets;

import app.controllers.UserController;
import app.controllers.AuthController;
import app.models.User;
import app.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {
    private UserController userController = new UserController();
    private AuthController authController = new AuthController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identity = request.getParameter("identity");
        String password = request.getParameter("password");
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // 1. Buscar al usuario por Email o Username
        User user = userController.findUser(identity);

        if (user == null) {
            // No revelamos detalles, pero auditamos el intento fallido
            authController.logActivity(0, "LOGIN_NOT_FOUND", ip, "Identidad no encontrada: " + identity);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
            return;
        }

        // 2. CAPA DE SEGURIDAD: Gestionar Penalizaciones (Baneo de 15 min)
        // Intentamos limpiar baneo si el tiempo ya pasó
        authController.checkAndResetPenalty(user.getEmails());
        // Recargamos el estado del usuario tras el posible reset
        user = userController.findUser(identity);

        if ("Ban".equalsIgnoreCase(user.getState())) {
            authController.logActivity(user.getIdUsers(), "LOGIN_BLOCKED", ip, "Acceso denegado por baneo activo.");
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=penalty");
            return;
        }

        if (!"Active".equalsIgnoreCase(user.getState())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=inactive");
            return;
        }

        // 3. CAPA DE VALIDACIÓN: BCrypt
        if (SecurityUtils.checkPassword(password, user.getPasswords())) {
            
            // LOGIN EXITOSO: Limpiar historial de fallos y resetear contador
            authController.resetAttempts(user.getEmails());
            
            // Seguridad de sesión: Destruir sesión vieja y crear una nueva
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            
            HttpSession session = request.getSession(true);
            session.setAttribute("userSession", user);
            
            authController.logActivity(user.getIdUsers(), "LOGIN_SUCCESS", ip, "Navegador: " + userAgent);
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            
        } else {
            // LOGIN FALLIDO: Aquí se activa la lógica de los 3 intentos
            authController.handleFailedAttempt(user.getEmails());
            authController.logActivity(user.getIdUsers(), "LOGIN_FAILED", ip, "Contraseña incorrecta para: " + user.getEmails());
            
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
        }
    }
}