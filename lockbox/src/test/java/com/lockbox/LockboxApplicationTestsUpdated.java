package com.lockbox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Main application test class that verifies the Spring context loads correctly
 * and all beans are properly configured.
 */
@SpringBootTest
@ActiveProfiles("test")
class LockboxApplicationTestsUpdated {

    /**
     * This test verifies that the Spring Boot application context loads successfully.
     * It ensures that:
     * - All configuration classes are valid
     * - All beans can be created and injected
     * - No circular dependencies exist
     * - Auto-configuration works properly
     */
    @Test
    void contextLoads() {
        // This test will fail if the Spring context cannot be loaded
        // No additional assertions needed - the context loading itself is the test
    }

    /**
     * Test that verifies the main method can be invoked without errors.
     * This is important for ensuring the application can start properly.
     */
    @Test
    void mainMethodTest() {
        // Test that the main method doesn't throw any exceptions during startup validation
        // Note: We don't actually start the full application here, just validate the main method
        try {
            LockboxApplication.main(new String[]{"--spring.main.web-environment=false"});
        } catch (Exception e) {
            // If we get here, there's a problem with the main method
            throw new AssertionError("Main method should not throw exceptions during basic validation", e);
        }
    }
} 