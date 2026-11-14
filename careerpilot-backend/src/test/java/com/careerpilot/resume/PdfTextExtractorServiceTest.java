package com.careerpilot.resume;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.modules.resume.service.PdfTextExtractorService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractorServiceTest {

    private PdfTextExtractorService extractorService;

    @BeforeEach
    void setUp() {
        extractorService = new PdfTextExtractorService();
    }

    @Test
    @DisplayName("Should extract selectable text from a valid PDF document")
    void testExtractTextSuccess() throws IOException {
        byte[] pdfBytes = createTestPdfWithText("John Developer\nSenior Software Engineer with Java and Spring Boot experience.");

        String extracted = extractorService.extractText(pdfBytes);

        assertNotNull(extracted);
        assertTrue(extracted.contains("John Developer"));
        assertTrue(extracted.contains("Senior Software Engineer"));
    }

    @Test
    @DisplayName("Should reject PDF when extracted text is below minimum length threshold")
    void testExtractTextInsufficientContent() throws IOException {
        byte[] pdfBytes = createTestPdfWithText("Hi");

        assertThrows(BadRequestException.class, () -> extractorService.extractText(pdfBytes));
    }

    private byte[] createTestPdfWithText(String content) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(content.replace("\n", " "));
                contentStream.endText();
            }

            doc.save(out);
            return out.toByteArray();
        }
    }
}

