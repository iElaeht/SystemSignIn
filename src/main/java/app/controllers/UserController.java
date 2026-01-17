package app.controllers;

import app.database.ConnectionDB;
import app.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserController {

    // 1. CREATE: Registro de nuevo usuario
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (UuidUser, Usernames, Passwords, Emails, Ip, Token, TokenExpiration, State) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUuidUser());
            ps.setString(2, user.getUsernames());
            ps.setString(3, user.getPasswords());
            ps.setString(4, user.getEmails());
            ps.setString(5, user.getIp());
            ps.setString(6, user.getToken());
            ps.setTimestamp(7, user.getTokenExpiration());
            ps.setString(8, user.getState() != null ? user.getState() : "Inactive");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // 2. READ: Buscar por Email o Username (Crucial para Login y validación de baneo)
    public User findUser(String identity) {
        String sql = "SELECT * FROM Users WHERE Emails = ? OR Usernames = ?";
        try (Connection conn = ConnectionDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identity);
            ps.setString(2, identity);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // 3. READ: Listar todo (Para Panel de Administración)
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY DateRegistration DESC";
        try (Connection conn = ConnectionDB.getConnection(); 
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // 4. UPDATE: Actualizar datos de perfil y estado de seguridad
    public boolean updateUser(User user) {
        // Añadimos Token y TokenExpiration a la consulta SQL
        String sql = "UPDATE Users SET Usernames = ?, Emails = ?, Roles = ?, State = ?, " +
                    "TokenAttempts = ?, PenaltyTime = ?, Token = ?, TokenExpiration = ? " + 
                    "WHERE UuidUser = ?";
                    
        try (Connection conn = ConnectionDB.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUsernames());
            ps.setString(2, user.getEmails());
            ps.setString(3, user.getRoles());
            ps.setString(4, user.getState());
            ps.setInt(5, user.getTokenAttempts());
            ps.setTimestamp(6, user.getPenaltyTime());
            // Nuevos parámetros vinculados
            ps.setString(7, user.getToken());
            ps.setTimestamp(8, user.getTokenExpiration());
            // El WHERE
            ps.setString(9, user.getUuidUser());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // 5. DELETE: Eliminación física por UUID
    public boolean deleteUser(String uuid) {
        String sql = "DELETE FROM Users WHERE UuidUser = ?";
        try (Connection conn = ConnectionDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    /**
     * MAPEO: Transfiere los datos de la fila de la BD al objeto Java User.
     * Incluye los campos de seguridad para el baneo de 15 minutos.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUsers(rs.getInt("IdUsers"));
        u.setUuidUser(rs.getString("UuidUser"));
        u.setUsernames(rs.getString("Usernames"));
        u.setPasswords(rs.getString("Passwords"));
        u.setEmails(rs.getString("Emails"));
        u.setIp(rs.getString("Ip"));
        u.setRoles(rs.getString("Roles"));
        u.setToken(rs.getString("Token"));
        u.setTokenExpiration(rs.getTimestamp("TokenExpiration"));
        
        // --- CAMPOS DE SEGURIDAD CRÍTICOS ---
        u.setTokenAttempts(rs.getInt("TokenAttempts")); 
        u.setPenaltyTime(rs.getTimestamp("PenaltyTime"));
        u.setState(rs.getString("State"));
        // ------------------------------------

        u.setLastLogin(rs.getTimestamp("LastLogin"));
        u.setDateRegistration(rs.getTimestamp("DateRegistration"));
        
        return u;
    }
}