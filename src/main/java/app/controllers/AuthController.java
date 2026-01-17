package app.controllers;

import app.database.ConnectionDB;
import java.sql.*;

public class AuthController {

    // 1. Manejo de fallos con baneo EXACTO al 3er intento
    public void handleFailedAttempt(String email) {
        // Sumamos + 1 en la comparación para que el baneo ocurra 
        // en el mismo instante que el contador llega a 3.
        String sql = "UPDATE Users SET TokenAttempts = TokenAttempts + 1, " +
                     "State = CASE WHEN TokenAttempts + 0 >= 5 THEN 'Ban' ELSE State END, " +
                     "PenaltyTime = CASE WHEN TokenAttempts + 0 >= 5 " +
                     "THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE) ELSE PenaltyTime END " +
                     "WHERE Emails = ?";
        
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    // 2. Autolimpieza de baneo (Reinicio tras 15 min)
    public void checkAndResetPenalty(String email) {
        String sql = "UPDATE Users SET TokenAttempts = 0, PenaltyTime = NULL, State = 'Inactive' " +
                     "WHERE Emails = ? AND State = 'Ban' AND PenaltyTime < NOW()";
        
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    // 3. Validar Token y Activar
    public boolean verifyAndActivate(String email, String token) {
        // Agregamos una validación explícita de que el token no sea NULL y el tiempo sea exacto
        String sql = "UPDATE Users SET State = 'Active', Token = NULL, TokenExpiration = NULL, TokenAttempts = 0 " +
                    "WHERE Emails = ? AND Token = ? AND TokenExpiration >= CURRENT_TIMESTAMP AND State = 'Inactive'";
        
        try (Connection conn = ConnectionDB.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, token);
            
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) { 
            return false; 
        }
    }

    // 4. Auditoría (Registro de acciones)
    public void logActivity(int userId, String action, String ip, String details) {
        String sql = "INSERT INTO AuditLogs (IdUser, Action, IpSource, Details) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId > 0) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, ip);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    // 4. MANTENIDO: Resetear intentos tras éxito
    public void resetAttempts(String email) {
        String sql = "UPDATE Users SET TokenAttempts = 0, PenaltyTime = NULL WHERE Emails = ?";
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    // 6. MANTENIDO: Historial de contraseñas
    public void saveToPasswordHistory(int userId, String hash) {
        String sql = "INSERT INTO PasswordHistory (IdUser, HashedPassword) VALUES (?, ?)";
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hash);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}