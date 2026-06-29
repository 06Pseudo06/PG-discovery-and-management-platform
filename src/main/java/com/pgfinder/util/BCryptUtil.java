package com.pgfinder.util;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verify(String plainPassword, String hash) {
        try {
            String normalizedHash = hash != null && hash.startsWith("$2b$")
                    ? "$2a$" + hash.substring(4)
                    : hash;
            return BCrypt.checkpw(plainPassword, normalizedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
