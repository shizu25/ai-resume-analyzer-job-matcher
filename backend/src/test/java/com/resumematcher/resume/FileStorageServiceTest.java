package com.resumematcher.resume;

import com.resumematcher.resume.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void testStoreValidPdf() throws IOException {
        // PDF magic bytes %PDF
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x35};
        MockMultipartFile file = new MockMultipartFile(
                "file", "my_resume.pdf", "application/pdf", pdfContent
        );

        FileStorageService.StorageResult result = fileStorageService.store(file);
        assertNotNull(result);
        assertEquals("my_resume.pdf", result.originalFilename());
        assertEquals("application/pdf", result.fileType());
        assertTrue(result.storedFileName().endsWith(".pdf"));
    }

    @Test
    void testStoreValidDocx() throws IOException {
        // ZIP magic bytes PK..
        byte[] docxContent = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00};
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxContent
        );

        FileStorageService.StorageResult result = fileStorageService.store(file);
        assertNotNull(result);
        assertEquals("resume.docx", result.originalFilename());
        assertTrue(result.storedFileName().endsWith(".docx"));
    }

    @Test
    void testStoreCorruptedMagicBytesThrowsException() {
        // Fake PDF with invalid bytes
        byte[] fakeContent = "This is not a real PDF file header".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", fakeContent
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.store(file);
        });

        assertTrue(exception.getMessage().contains("does not match a valid PDF or DOCX"));
    }

    @Test
    void testStoreEmptyFileThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.store(file);
        });
    }

    @Test
    void testPreventPathTraversal() {
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x35};
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/passwd.pdf", "application/pdf", pdfContent
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.store(file);
        });
    }
}
