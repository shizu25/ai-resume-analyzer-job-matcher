package com.resumematcher.resume.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // Magic numbers
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK.. (DOCX is a zip archive)

    public FileStorageService(@Value("${app.storage.resume-dir:storage/resumes}") String resumeDir) {
        this.storageDirectory = Paths.get(resumeDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize local resume storage folder at: " + this.storageDirectory, e);
        }
    }

    public StorageResult store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum permitted limit of 10MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume");
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.equals("pdf") && !extension.equals("docx")) {
            throw new IllegalArgumentException("Invalid file type. Only PDF (.pdf) and Microsoft Word (.docx) files are supported");
        }

        // Validate magic bytes
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new IllegalArgumentException("File is corrupted or truncated");
            }
        }

        String detectedType;
        if (Arrays.equals(header, PDF_MAGIC)) {
            detectedType = "application/pdf";
        } else if (Arrays.equals(header, ZIP_MAGIC)) {
            detectedType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else {
            throw new IllegalArgumentException("File content does not match a valid PDF or DOCX format");
        }

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path targetLocation = this.storageDirectory.resolve(storedFileName).normalize();

        if (!targetLocation.startsWith(this.storageDirectory)) {
            throw new SecurityException("Cannot store file outside current storage directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StorageResult(originalFilename, storedFileName, targetLocation.toString(), detectedType, file.getSize());
    }

    public Resource loadAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath).normalize();
            if (!file.startsWith(this.storageDirectory)) {
                throw new SecurityException("Access to path outside storage directory is forbidden: " + filePath);
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("File not found: " + filePath, e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null) {
                Path file = Paths.get(filePath).normalize();
                if (file.startsWith(this.storageDirectory)) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            // Log and allow flow to continue
            System.err.println("Could not delete physical file: " + filePath + ", reason: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    public record StorageResult(String originalFilename, String storedFileName, String filePath, String fileType, long fileSize) {}
}
