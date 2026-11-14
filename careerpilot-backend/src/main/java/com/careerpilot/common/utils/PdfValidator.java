package com.careerpilot.common.utils;

import com.careerpilot.common.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class PdfValidator {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-

    private PdfValidator() {
    }

    public static void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a PDF file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 5 MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Invalid file type. Only PDF documents (.pdf) are supported");
        }

        // Validate Magic Bytes (File Header Signature)
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[5];
            int read = inputStream.read(header);

            if (read < 5 || !Arrays.equals(header, PDF_MAGIC_BYTES)) {
                throw new BadRequestException("Corrupted or invalid PDF file. File signature does not match PDF format");
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file: " + e.getMessage());
        }
    }
}

