package com.careerpilot;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CareerPilotApplicationTest {

    @Test
    void testFindDotEnvPathResolvesRootEnv() {
        Path path = CareerPilotApplication.findDotEnvPath();
        assertNotNull(path, ".env file should be resolved from root or current directory");
        assertTrue(path.toFile().exists(), ".env file must exist on disk");
        assertTrue(path.getFileName().toString().endsWith(".env"), "Resolved file must be .env");
    }

    @Test
    void testLoadDotEnvPopulatesSystemProperties() {
        CareerPilotApplication.loadDotEnv();

        String geminiKey = System.getProperty("GEMINI_API_KEY");
        assertNotNull(geminiKey, "GEMINI_API_KEY should be loaded into System properties");
        assertFalse(geminiKey.isBlank(), "GEMINI_API_KEY should not be blank");

        String chatModel = System.getProperty("GEMINI_CHAT_MODEL");
        assertNotNull(chatModel, "GEMINI_CHAT_MODEL should be loaded into System properties");
        assertEquals("gemini-3.8-flash", chatModel);
    }

    @Test
    void testPrecedenceDoesNotOverwriteExistingProperties() {
        System.setProperty("TEST_PREEXISTING_KEY", "original_value");
        try {
            CareerPilotApplication.loadDotEnv();
            assertEquals("original_value", System.getProperty("TEST_PREEXISTING_KEY"));
        } finally {
            System.clearProperty("TEST_PREEXISTING_KEY");
        }
    }
}

