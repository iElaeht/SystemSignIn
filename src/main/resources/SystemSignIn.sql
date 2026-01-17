-- 1. Crear la base de datos (opcional, por si no la has creado)
DROP DATABASE IF EXISTS SystemSignIn;
CREATE DATABASE IF NOT EXISTS SystemSignIn;
USE SystemSignIn;

-- 2. Tabla de Usuarios: El corazón del sistema
CREATE TABLE Users (
    IdUsers INT AUTO_INCREMENT PRIMARY KEY,
    UuidUser VARCHAR(36) NOT NULL UNIQUE,
    Usernames VARCHAR(40) NOT NULL UNIQUE,
    Passwords VARCHAR(255) NOT NULL,
    Emails VARCHAR(80) NOT NULL UNIQUE,
    Ip VARCHAR(60) NOT NULL,
    Roles VARCHAR(70) NOT NULL DEFAULT 'User',
    -- Control de Verificación (Token de 9 dígitos)
    Token VARCHAR(9) DEFAULT NULL,
    TokenExpiration DATETIME DEFAULT NULL,
    -- Rate Limiting (Bloqueo de 15 minutos)
    TokenAttempts INT DEFAULT 0,
    PenaltyTime DATETIME DEFAULT NULL,  
    -- Trazabilidad y Estado
    LastLogin DATETIME DEFAULT NULL,
    DateRegistration DATETIME DEFAULT CURRENT_TIMESTAMP,
    State ENUM('Active', 'Inactive', 'Ban') NOT NULL DEFAULT 'Inactive'
) ENGINE=INNODB;

-- 3. Tabla de Auditoría: Registro de actividad (Audit Log)
CREATE TABLE AuditLogs (
    IdLog INT AUTO_INCREMENT PRIMARY KEY,
    IdUser INT,
    Action VARCHAR(100) NOT NULL,
    IpSource VARCHAR(60), 
    UserAgent VARCHAR(255),
    Details TEXT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUser) REFERENCES Users(IdUsers) ON DELETE SET NULL
) ENGINE=INNODB;

-- 4. Tabla de Historial de Contraseñas: Seguridad Pro (No repetir últimas 3)
CREATE TABLE PasswordHistory (
    IdHistory INT AUTO_INCREMENT PRIMARY KEY,
    IdUser INT NOT NULL,
    HashedPassword VARCHAR(255) NOT NULL,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUser) REFERENCES Users(IdUsers) ON DELETE CASCADE
) ENGINE=INNODB;

SELECT * FROM users;
SELECT * FROM AuditLogs;

-- UPDATE Users SET TokenExpiration = DATE_SUB(NOW(), INTERVAL 11 MINUTE) WHERE Emails = 'elaeht2002@gmail.com';
-- UPDATE Users SET PenaltyTime = DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE Emails = 'elaeht2002@gmail.com';