package com.resumematcher.resume;

import com.resumematcher.resume.dto.ResumeDetailDto;
import com.resumematcher.resume.dto.ResumeResponseDto;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.*;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.resume.service.FileStorageService;
import com.resumematcher.resume.service.ResumeService;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeSkillRepository skillRepository;
    @Mock private ResumeEducationRepository educationRepository;
    @Mock private ResumeExperienceRepository experienceRepository;
    @Mock private ResumeProjectRepository projectRepository;
    @Mock private ResumeSuggestionRepository suggestionRepository;
    @Mock private ResumeWarningRepository warningRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private AiServiceClient aiServiceClient;

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(
                resumeRepository, skillRepository, educationRepository, experienceRepository,
                projectRepository, suggestionRepository, warningRepository, userRepository,
                fileStorageService, aiServiceClient
        );
    }

    @Test
    void testUploadResumeSuccess() throws IOException {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(fileStorageService.store(any())).thenReturn(
                new FileStorageService.StorageResult("resume.pdf", "stored.pdf", "/storage/resumes/stored.pdf", "application/pdf", 1024L)
        );
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.empty());

        Resume savedResume = new Resume();
        savedResume.setId(UUID.randomUUID());
        savedResume.setFileName("resume.pdf");
        savedResume.setFileType("application/pdf");
        savedResume.setFileSize(1024L);
        savedResume.setIsActive(true);
        savedResume.setParsingStatus("UPLOADED");

        when(resumeRepository.save(any(Resume.class))).thenReturn(savedResume);

        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});
        ResumeResponseDto result = resumeService.uploadResume(file, "user@example.com");

        assertNotNull(result);
        assertEquals("resume.pdf", result.getFileName());
        assertEquals("UPLOADED", result.getParsingStatus());
        assertTrue(result.getIsActive());
    }

    @Test
    void testUnauthorizedResumeAccessThrowsException() {
        UUID ownerId = UUID.randomUUID();
        User owner = new User();
        owner.setId(ownerId);

        UUID unauthorizedUserId = UUID.randomUUID();
        User unauthorizedUser = new User();
        unauthorizedUser.setId(unauthorizedUserId);
        unauthorizedUser.setEmail("other@example.com");

        UUID resumeId = UUID.randomUUID();
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setUser(owner);

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        assertThrows(AccessDeniedException.class, () -> {
            resumeService.getResumeDetail(resumeId, "other@example.com");
        });
    }

    @Test
    void testDeleteResumeCleansUpFileAndDb() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");

        UUID resumeId = UUID.randomUUID();
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setUser(user);
        resume.setFilePath("/storage/resumes/my_resume.pdf");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        resumeService.deleteResume(resumeId, "user@example.com");

        verify(fileStorageService, times(1)).deleteFile("/storage/resumes/my_resume.pdf");
        verify(resumeRepository, times(1)).delete(resume);
    }
}
