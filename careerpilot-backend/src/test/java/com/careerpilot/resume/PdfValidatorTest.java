package com.careerpilot.resume;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.utils.PdfValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfValidatorTest {

    private static final byte[] VALID_PDF_HEADER = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34}; // %PDF-1.4

    @Test
    @DisplayName("Should successfully validate a valid PDF file")
    void testValidPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                VALID_PDF_HEADER
        );

        assertDoesNotThrow(() -> PdfValidator.validatePdf(file));
    }

    @Test
    @DisplayName("Should reject file when original filename does not end in .pdf")
    void testNonPdfExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                VALID_PDF_HEADER
        );

        assertThrows(BadRequestException.class, () -> PdfValidator.validatePdf(file));
    }

    @Test
    @DisplayName("Should reject file with .pdf extension but invalid magic bytes")
    void testInvalidMagicBytes() {
        byte[] fakeBytes = "NOT A REAL PDF FILE HEADER".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                fakeBytes
        );

        assertThrows(BadRequestException.class, () -> PdfValidator.validatePdf(file));
    }

    @Test
    @DisplayName("Should reject file exceeding 5 MB limit")
    void testOversizedPdf() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6 MB
        System.arraycopy(VALID_PDF_HEADER, 0, largeBytes, 0, VALID_PDF_HEADER.length);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "huge.pdf",
                "application/pdf",
                largeBytes
        );

        assertThrows(BadRequestException.class, () -> PdfValidator.validatePdf(file));
    }
}

