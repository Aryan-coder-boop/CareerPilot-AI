package com.careerpilot.modules.resume.service;

import com.careerpilot.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootStoragePath;

    public FileStorageService(@Value("${careerpilot.storage.upload-dir:./uploads/resumes}") String uploadDir) {
        this.rootStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStoragePath);
        } catch (IOException e) {
            logger.error("Failed to initialize storage directory at {}", this.rootStoragePath, e);
            throw new RuntimeException("Could not create resume upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, UUID resumeId) {
        String storedFileName = "resume_" + resumeId + ".pdf";
        Path targetLocation = this.rootStoragePath.resolve(storedFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
        } catch (IOException e) {
            logger.error("Failed to store file {}", storedFileName, e);
            throw new BadRequestException("Failed to store file on disk: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null) return;
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Could not delete stored file at path: {}", filePath, e);
        }
    }

    public Path getRootStoragePath() {
        return rootStoragePath;
    }
}

