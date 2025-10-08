package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int COST = 12; // độ “mặn” (work factor)

    public static String hashPassword(String plain) {
        if (plain == null) throw new IllegalArgumentException("plain password null");
        return BCrypt.hashpw(plain, BCrypt.gensalt(COST));
    }

    public static boolean matches(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        // Nếu hashed có format BCrypt hợp lệ
        if (hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$")) {
            try { return BCrypt.checkpw(plain, hashed); }
            catch (Exception ignore) { return false; }
        }
        // Trường hợp cũ: DB còn plaintext -> so sánh thường (để hỗ trợ nâng cấp dần)
        return plain.equals(hashed);
    }

    public static boolean isBCrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
