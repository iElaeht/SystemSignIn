package app.controllers;

import app.database.ConnectionDB;
import app.models.Audit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditController {

    // 1. CREATE: Registrar una nueva acción (Automático)
    public void logAction(int idUser, String action, String ip, String userAgent, String details) {
        String sql = "INSERT INTO AuditLogs (IdUser, Action, IpSource, UserAgent, Details) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (idUser > 0) {
                ps.setInt(1, idUser);
            } else {
                ps.setNull(1, Types.INTEGER); // Para acciones de usuarios no logueados (ej: intentos fallidos)
            }
            
            ps.setString(2, action);
            ps.setString(3, ip);
            ps.setString(4, userAgent);
            ps.setString(5, details);
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar auditoría: " + e.getMessage());
        }
    }

    // 2. READ: Listar logs para el Panel de Administración
    public List<Audit> getAllLogs() {
        List<Audit> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs ORDER BY CreatedAt DESC LIMIT 500"; // Últimos 500 movimientos
        
        try (Connection conn = ConnectionDB.getConnection(); 
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Audit log = new Audit();
                log.setIdLog(rs.getInt("IdLog"));
                log.setIdUser(rs.getInt("IdUser"));
                log.setAction(rs.getString("Action"));
                log.setIpSource(rs.getString("IpSource"));
                log.setUserAgent(rs.getString("UserAgent"));
                log.setDetails(rs.getString("Details"));
                log.setCreatedAt(rs.getTimestamp("CreatedAt"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    // 3. READ: Buscar logs de un usuario específico
    public List<Audit> getLogsByUser(int idUser) {
        List<Audit> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogs WHERE IdUser = ? ORDER BY CreatedAt DESC";
        
        try (Connection conn = ConnectionDB.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // (Mismo mapeo que arriba)
                Audit log = new Audit();
                log.setIdLog(rs.getInt("IdLog"));
                log.setAction(rs.getString("Action"));
                log.setCreatedAt(rs.getTimestamp("CreatedAt"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}