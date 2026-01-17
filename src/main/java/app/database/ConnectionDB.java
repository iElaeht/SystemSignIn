package app.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    // Cargamos Dotenv de forma estática para que esté disponible siempre
    private static final Dotenv dotenv = Dotenv.load();

    // Obtenemos los valores directamente del archivo .env
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // El driver moderno es com.mysql.cj.jdbc.Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Usamos las variables cargadas desde el .env
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el Driver (revisa tu pom.xml): " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo conectar a MySQL. Revisa tu archivo .env y el servicio de MySQL.");
            System.err.println("Detalle: " + e.getMessage());
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }

    // TEST DE CONEXIÓN REAL POR TERMINAL
    public static void main(String[] args) {
        System.out.println("Intentando conectar usando variables del archivo .env...");
        
        try (Connection c = getConnection()) {
            if (c != null) {
                System.out.println("*********************************");
                System.out.println("¡CONEXIÓN EXITOSA!");
                System.out.println("Servidor: " + c.getMetaData().getDatabaseProductVersion());
                System.out.println("Base de datos conectada correctamente.");
                System.out.println("*********************************");
            } else {
                System.out.println("Falló la conexión. Verifica que el archivo .env esté en la raíz.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}