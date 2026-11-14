package com.careerpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class CareerPilotApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(CareerPilotApplication.class, args);
    }

    /**
     * Safely loads properties from the root .env file into System properties before
     * the Spring context initializes.
     * Guarantees:
     * - Searches current directory and parent directory (supporting run from careerpilot-backend).
     * - Existing OS environment variables and system properties take precedence.
     * - Never logs or exposes secret values.
     */
    static void loadDotEnv() {
        Path envPath = findDotEnvPath();
        if (envPath == null) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int eqIndex = line.indexOf('=');
                if (eqIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, eqIndex).trim();
                String value = line.substring(eqIndex + 1).trim();

                // Strip surrounding single or double quotes if present
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }

                // Existing OS environment variables and JVM system properties take precedence
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            // Silently continue or log safe generic notice without leaking secrets
            System.err.println("Warning: Could not read .env configuration file");
        }
    }

    static Path findDotEnvPath() {
        Path current = Paths.get("").toAbsolutePath();

        // 1. Check current working directory
        Path direct = current.resolve(".env");
        if (Files.isRegularFile(direct)) {
            return direct;
        }

        // 2. Check parent directory (when executing from careerpilot-backend directory)
        Path parent = current.getParent();
        if (parent != null) {
            Path fromParent = parent.resolve(".env");
            if (Files.isRegularFile(fromParent)) {
                return fromParent;
            }
        }

        // 3. Fallback relative paths
        Path relCurrent = Paths.get(".env");
        if (Files.isRegularFile(relCurrent)) {
            return relCurrent;
        }
        Path relParent = Paths.get("..", ".env");
        if (Files.isRegularFile(relParent)) {
            return relParent;
        }

        return null;
    }
}
