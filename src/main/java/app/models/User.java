package app.models;

import java.sql.Timestamp;

public class User {
    private int idUsers;
    private String uuidUser;
    private String usernames;
    private String passwords;
    private String emails;
    private String ip;
    private String roles;
    private String token;
    private Timestamp tokenExpiration;
    private int tokenAttempts;
    private Timestamp penaltyTime;
    private Timestamp lastLogin;
    private Timestamp dateRegistration;
    private String state; // Active, Inactive, Ban

    public User() {}

    // Getters y Setters de TODOS los campos
    public int getIdUsers() { return idUsers; }
    public void setIdUsers(int idUsers) { this.idUsers = idUsers; }

    public String getUuidUser() { return uuidUser; }
    public void setUuidUser(String uuidUser) { this.uuidUser = uuidUser; }

    public String getUsernames() { return usernames; }
    public void setUsernames(String usernames) { this.usernames = usernames; }

    public String getPasswords() { return passwords; }
    public void setPasswords(String passwords) { this.passwords = passwords; }

    public String getEmails() { return emails; }
    public void setEmails(String emails) { this.emails = emails; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Timestamp getTokenExpiration() { return tokenExpiration; }
    public void setTokenExpiration(Timestamp tokenExpiration) { this.tokenExpiration = tokenExpiration; }

    public int getTokenAttempts() { return tokenAttempts; }
    public void setTokenAttempts(int tokenAttempts) { this.tokenAttempts = tokenAttempts; }

    public Timestamp getPenaltyTime() { return penaltyTime; }
    public void setPenaltyTime(Timestamp penaltyTime) { this.penaltyTime = penaltyTime; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public Timestamp getDateRegistration() { return dateRegistration; }
    public void setDateRegistration(Timestamp dateRegistration) { this.dateRegistration = dateRegistration; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}