package app.models;

import java.sql.Timestamp;

public class Audit {
    private int idLog;
    private int idUser;
    private String action;
    private String ipSource;
    private String userAgent;
    private String details;
    private Timestamp createdAt;

    public Audit() {}

    // Getters y Setters
    public int getIdLog() { return idLog; }
    public void setIdLog(int idLog) { this.idLog = idLog; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getIpSource() { return ipSource; }
    public void setIpSource(String ipSource) { this.ipSource = ipSource; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}