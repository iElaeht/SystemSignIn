package app.models;

import java.sql.Timestamp;

public class PasswordHistory {
    private int idHistory;
    private int idUser;
    private String hashedPassword;
    private Timestamp createdAt;

    public PasswordHistory() {}

    // Getters y Setters
    public int getIdHistory() { return idHistory; }
    public void setIdHistory(int idHistory) { this.idHistory = idHistory; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}