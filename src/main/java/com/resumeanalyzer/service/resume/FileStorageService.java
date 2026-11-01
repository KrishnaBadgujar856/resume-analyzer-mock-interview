package com.resumeanalyzer.service.resume;

import com.resumeanalyzer.entity.FileType;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.DocumentProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadLocation;

    public FileStorageService(@Value("${app.upload.dir:./uploads/resumes}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new DocumentProcessingException("Could not initialize upload directory at: " + uploadDir, e);
        }
    }

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public StoredFileResult storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty. Please select a valid resume file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds the 10MB limit. Please upload a smaller resume file.");
        }

        String rawOriginalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume");
        if (rawOriginalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + rawOriginalFilename);
        }

        FileType fileType = detectFileType(rawOriginalFilename);
        if (fileType == FileType.UNKNOWN) {
            throw new BadRequestException("Unsupported file type. Only PDF (.pdf) and Word (.docx) documents are accepted.");
        }

        validateFileHeader(file, fileType);

        String extension = fileType == FileType.PDF ? ".pdf" : ".docx";
        String uniqueStoredFilename = UUID.randomUUID() + "_" + System.currentTimeMillis() + extension;

        try {
            Path targetLocation = this.uploadLocation.resolve(uniqueStoredFilename).normalize();
            if (!targetLocation.startsWith(this.uploadLocation)) {
                throw new BadRequestException("Invalid path sequence detected.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFileResult(rawOriginalFilename, uniqueStoredFilename, targetLocation, fileType, file.getSize());
        } catch (IOException ex) {
            throw new DocumentProcessingException("Failed to store uploaded file: " + rawOriginalFilename, ex);
        }
    }

    public Path getFilePath(String storedFilename) {
        Path path = this.uploadLocation.resolve(storedFilename).normalize();
        if (!path.startsWith(this.uploadLocation)) {
            throw new BadRequestException("Invalid file access attempted.");
        }
        return path;
    }

    public void deleteFile(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) return;
        try {
            Path path = getFilePath(storedFilename);
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Logged or ignored safely
        }
    }

    private FileType detectFileType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return FileType.PDF;
        } else if (lower.endsWith(".docx")) {
            return FileType.DOCX;
        }
        return FileType.UNKNOWN;
    }

    private void validateFileHeader(MultipartFile file, FileType fileType) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[Math.min(1024, (int) Math.max(4, file.getSize()))];
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new BadRequestException("The uploaded file is too small to be a valid " + fileType + " document.");
            }

            if (fileType == FileType.PDF) {
                String headerStr = new String(header, 0, bytesRead, java.nio.charset.StandardCharsets.ISO_8859_1);
                if (!headerStr.contains("%PDF")) {
                    throw new BadRequestException("The uploaded file does not contain a valid PDF signature (%PDF). Please ensure it is a valid PDF document.");
                }
            } else if (fileType == FileType.DOCX) {
                if (header[0] != 0x50 || header[1] != 0x4B) { // 'P', 'K'
                    throw new BadRequestException("The uploaded file does not contain a valid Word (.docx) package signature (PK). Please ensure it is a valid DOCX document.");
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read uploaded file signature.", e);
        }
    }

    public static class StoredFileResult {
        private final String originalFilename;
        private final String storedFilename;
        private final Path path;
        private final FileType fileType;
        private final long size;

        public StoredFileResult(String originalFilename, String storedFilename, Path path, FileType fileType, long size) {
            this.originalFilename = originalFilename;
            this.storedFilename = storedFilename;
            this.path = path;
            this.fileType = fileType;
            this.size = size;
        }

        public String getOriginalFilename() { return originalFilename; }
        public String getStoredFilename() { return storedFilename; }
        public Path getPath() { return path; }
        public FileType getFileType() { return fileType; }
        public long getSize() { return size; }
    }
}
