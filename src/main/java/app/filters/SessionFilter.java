package app.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class SessionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // 1. Obtener la ruta que el usuario está pidiendo
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 2. Definir rutas que NO necesitan login (Rutas Públicas)
        // Agregamos "/" y "/index.jsp" para que la página de inicio sea accesible
        boolean isPublicPage = path.equals("/login.jsp") || 
                               path.equals("/register.jsp") || 
                               path.equals("/verify.jsp") ||
                               path.equals("/") || 
                               path.equals("/index.jsp");

        // 3. Definir rutas de los SERVLETS públicos (donde envías los formularios)
        boolean isPublicServlet = path.startsWith("/auth/") || 
                                  path.startsWith("/verify");

        // 4. Recursos estáticos (CSS, JS, Imágenes)
        boolean isResource = path.startsWith("/css/") || 
                             path.startsWith("/js/") || 
                             path.startsWith("/img/");

        // 5. Verificar si el usuario está logueado
        boolean loggedIn = (session != null && session.getAttribute("userSession") != null);

        // LÓGICA DE ACCESO
        if (loggedIn || isPublicPage || isPublicServlet || isResource) {
            // Permitir el paso
            chain.doFilter(request, response);
        } else {
            // Si intenta entrar a algo privado sin sesión, mandarlo al login
            res.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}