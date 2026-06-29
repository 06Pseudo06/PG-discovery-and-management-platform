package com.pgfinder.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptSeedTest {

    @Test
    void seedHashMatchesPassword123() {
        String seedHash = "$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G";
        assertTrue(BCryptUtil.verify("password123", seedHash),
                "Seed password hash must verify with password123 for demo logins");
    }
}
