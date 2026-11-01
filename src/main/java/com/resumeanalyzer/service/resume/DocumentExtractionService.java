package com.resumeanalyzer.service.resume;

import com.resumeanalyzer.entity.FileType;
import com.resumeanalyzer.exception.DocumentProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class DocumentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractionService.class);

    public String extractText(Path filePath, FileType fileType) {
        if (filePath == null) {
            throw new DocumentProcessingException("File path cannot be null.");
        }

        File file = filePath.toFile();
        if (!file.exists()) {
            throw new DocumentProcessingException("Document file not found on disk.");
        }

        String extractedText;
        if (fileType == FileType.PDF) {
            extractedText = extractFromPdf(file);
        } else if (fileType == FileType.DOCX) {
            extractedText = extractFromDocx(file);
        } else {
            throw new DocumentProcessingException("Unsupported file type for extraction: " + fileType);
        }

        extractedText = sanitizeExtractedText(extractedText);

        // Check for scanned, image-only, or insufficient readable document text (< 50 non-whitespace characters)
        if (extractedText.isBlank() || extractedText.replaceAll("\\s+", "").length() < 50) {
            if (fileType == FileType.PDF) {
                throw new DocumentProcessingException(
                        "The uploaded PDF contains insufficient selectable text (fewer than 50 characters). " +
                        "It appears to be a scanned document or image-based PDF. " +
                        "OCR (Optical Character Recognition) is not supported in this environment. " +
                        "Please upload a standard text-based PDF (with selectable, copyable text) or a Word (.docx) document."
                );
            } else {
                throw new DocumentProcessingException(
                        "The uploaded Word (.docx) document contains insufficient readable text (fewer than 50 characters). " +
                        "Please upload a complete resume document with readable text sections."
                );
            }
        }

        return extractedText;
    }

    private String extractFromPdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) {
                throw new DocumentProcessingException("The uploaded PDF is password protected. Please upload an unencrypted document.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            throw new DocumentProcessingException("The uploaded PDF is password protected. Please upload an unencrypted document.", e);
        } catch (DocumentProcessingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to extract text from PDF: {}", file.getName(), e);
            throw new DocumentProcessingException("Failed to read PDF document. The file may be damaged, corrupted, or not a valid PDF.", e);
        }
    }

    private String extractFromDocx(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        } catch (Exception e) {
            logger.error("Failed to extract text from DOCX: {}", file.getName(), e);
            throw new DocumentProcessingException("Failed to read Word (.docx) document. Please ensure the file is an uncorrupted DOCX document.", e);
        }
    }

    private String sanitizeExtractedText(String text) {
        if (text == null) return "";
        // Remove null bytes, normalize carriage returns and consecutive whitespaces
        return text.replace("\0", "")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[\t ]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
