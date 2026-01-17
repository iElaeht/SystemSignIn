package app.utils;

import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;
import java.security.SecureRandom;

public class SecurityUtils {

    /**
     * Genera un ID único universal (UUID) para el campo UuidUser.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Encripta la contraseña usando BCrypt.
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verifica si una contraseña plana coincide con el hash de la BD.
     */
    public static boolean checkPassword(String password, String hashed) {
        try {
            return BCrypt.checkpw(password, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera el Token de 9 dígitos para la verificación por correo.
     */
    public static String generateNineDigitToken() {
        SecureRandom random = new SecureRandom();
        // Genera un número entre 100,000,000 y 999,999,999
        int number = 100000000 + random.nextInt(900000000);
        return String.valueOf(number);
    }
}