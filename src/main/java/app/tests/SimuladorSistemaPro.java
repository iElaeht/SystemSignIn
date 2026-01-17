package app.tests;

import app.controllers.*;
import app.models.User;
import app.services.EmailService;
import app.utils.SecurityUtils;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Scanner;

public class SimuladorSistemaPro {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserController userCtrl = new UserController();
        AuthController authCtrl = new AuthController();
        AuditController auditCtrl = new AuditController();
        EmailService emailService = new EmailService();

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String userAgent = "Console-OTP-Secure";

            System.out.println("=== SISTEMA DE TOKEN DINAMICO POR INTENTO ===");
            System.out.print("Nombre de Usuario: "); String name = sc.nextLine();
            System.out.print("Email: "); String email = sc.nextLine();
            System.out.print("Password: "); String pass = sc.nextLine();

            authCtrl.checkAndResetPenalty(email);
            
            // Fresh start para el test
            User existente = userCtrl.findUser(email);
            if (existente != null) userCtrl.deleteUser(existente.getUuidUser());

            // Registro inicial
            User u = new User();
            u.setUuidUser(SecurityUtils.generateUUID());
            u.setUsernames(name);
            u.setEmails(email);
            u.setPasswords(SecurityUtils.hashPassword(pass));
            u.setIp(ip);
            u.setToken(SecurityUtils.generateNineDigitToken());
            u.setTokenExpiration(new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000)));
            u.setState("Inactive");

            if (userCtrl.registerUser(u)) {
                System.out.println("\n[SISTEMA]: Registro exitoso. Enviando primer token...");
                emailService.sendToken(email, u.getToken());

                boolean procesoActivo = true;
                while (procesoActivo) {
                    User db = userCtrl.findUser(email);
                    if (db == null) break;

                    // Módulo de Penalización
                    if ("Ban".equalsIgnoreCase(db.getState())) {
                        Timestamp ahora = new Timestamp(System.currentTimeMillis());
                        if (db.getPenaltyTime() != null && ahora.before(db.getPenaltyTime())) {
                            long diff = db.getPenaltyTime().getTime() - ahora.getTime();
                            System.out.println("\n🚫 BLOQUEADO. Intenta en " + ((diff / 60000) + 1) + " min.");
                            procesoActivo = false; break;
                        } else {
                            authCtrl.checkAndResetPenalty(email);
                            db = userCtrl.findUser(email);
                        }
                    }

                    System.out.println("\n--- VERIFICACION (Intento " + (db.getTokenAttempts() + 1) + " de 3) ---");
                    System.out.print("➤ Ingrese token: ");
                    String input = sc.nextLine();

                    // 1. Intentar validar
                    if (authCtrl.verifyAndActivate(email, input)) {
                        System.out.println("✅ EXITO: Cuenta activada.");
                        auditCtrl.logAction(db.getIdUsers(), "ACCOUNT_VERIFIED", ip, userAgent, "Correcto");
                        procesoActivo = false;
                    } else {
                        // 2. LOGICA DE REGENERACION AUTOMATICA POR FALLO
                        System.out.println("❌ TOKEN INCORRECTO O EXPIRADO.");
                        authCtrl.handleFailedAttempt(email); // Aumenta contador y banea si llega a 3
                        
                        // Volvemos a consultar para ver si el fallo causó baneo
                        db = userCtrl.findUser(email);
                        if (!"Ban".equals(db.getState())) {
                            System.out.println("⚠️ Por seguridad, el token anterior ha sido invalidado.");
                            
                            // Generamos y guardamos el NUEVO token
                            String nuevoToken = SecurityUtils.generateNineDigitToken();
                            db.setToken(nuevoToken);
                            db.setTokenExpiration(new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000)));
                            
                            if(userCtrl.updateUser(db)) {
                                emailService.sendToken(email, nuevoToken);
                                System.out.println("📬 SE HA ENVIADO UN NUEVO TOKEN A TU CORREO.");
                                System.out.println("Usa el nuevo codigo para el siguiente intento.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { sc.close(); }
    }
}