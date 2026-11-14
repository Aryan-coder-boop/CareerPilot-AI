package com.careerpilot.modules.resume.service;

import com.careerpilot.common.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfTextExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(PdfTextExtractorService.class);
    private static final int MAX_ALLOWED_PAGES = 10;
    private static final int MIN_TEXT_LENGTH_THRESHOLD = 50;

    public String extractText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return extractText(bytes);
        } catch (IOException e) {
            logger.error("Error reading bytes from multipart file", e);
            throw new BadRequestException("Failed to read PDF file: " + e.getMessage());
        }
    }

    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new BadRequestException("Password-protected or encrypted PDFs are not supported. Please remove the password and re-upload.");
            }

            int pageCount = document.getNumberOfPages();
            if (pageCount > MAX_ALLOWED_PAGES) {
                throw new BadRequestException(
                        String.format("PDF exceeds maximum allowed page count (%d pages). Resume must be %d pages or fewer.",
                                pageCount, MAX_ALLOWED_PAGES)
                );
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            if (text == null || text.trim().length() < MIN_TEXT_LENGTH_THRESHOLD) {
                throw new BadRequestException(
                        "The uploaded PDF contains insufficient selectable text. Scanned or image-only PDFs are not supported. Please upload a PDF generated from Word, Docs, or LaTeX."
                );
            }

            return text.trim();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to extract text from PDF document", e);
            throw new BadRequestException("Failed to extract text from PDF: " + e.getMessage());
        }
    }
}

