package com.resumematcher.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.simulator.dto.*;
import com.resumematcher.simulator.entity.ResumeImprovementSuggestion;
import com.resumematcher.simulator.entity.ResumeVersion;
import com.resumematcher.simulator.repository.ResumeImprovementSuggestionRepository;
import com.resumematcher.simulator.repository.ResumeVersionRepository;
import com.resumematcher.simulator.service.ResumeVersionService;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeVersionServiceTest {

    @Mock
    private ResumeVersionRepository versionRepository;

    @Mock
    private ResumeImprovementSuggestionRepository suggestionRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResumeVersionService versionService;

    private User testUser;
    private Resume testResume;
    private Job testJob;
    private UUID userId;
    private UUID resumeId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        versionService = new ResumeVersionService(
                versionRepository, suggestionRepository, resumeRepository, jobRepository,
                userRepository, aiServiceClient, objectMapper
        );

        userId = UUID.randomUUID();
        resumeId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testResume = new Resume();
        testResume.setId(resumeId);
        testResume.setUser(testUser);
        testResume.setFileName("original_resume.pdf");
        testResume.setParsedText("Original resume raw text");

        testJob = new Job();
        testJob.setId(jobId);
        testJob.setTitle("Software Engineer");
        testJob.setDescription("Java and Spring Boot developer");
        testJob.setRequiredSkills(List.of("Java", "Spring Boot"));
    }

    @Test
    void createVersion_incrementsVersionNumber() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(testResume));
        when(versionRepository.findMaxVersionNumberByParentResumeId(resumeId)).thenReturn(2);
        when(versionRepository.save(any(ResumeVersion.class))).thenAnswer(inv -> {
            ResumeVersion v = inv.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        CreateResumeVersionRequest req = new CreateResumeVersionRequest("Tailored for Acme", null, "{}", "Sample text");
        ResumeVersionDto dto = versionService.createVersion(resumeId, req, "test@example.com");

        assertNotNull(dto);
        assertEquals(3, dto.getVersionNumber());
        assertEquals("Tailored for Acme", dto.getVersionName());
        verify(versionRepository, times(1)).save(any());
    }

    @Test
    void updateVersion_updatesContent() {
        ResumeVersion version = new ResumeVersion();
        UUID versionId = UUID.randomUUID();
        version.setId(versionId);
        version.setUser(testUser);
        version.setParentResume(testResume);
        version.setVersionNumber(1);
        version.setVersionName("Draft 1");
        version.setContentJson("{\"summary\":\"old\"}");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(versionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(ResumeVersion.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateResumeVersionRequest req = new UpdateResumeVersionRequest("Draft 1 Updated", "{\"summary\":\"new\"}", null);
        ResumeVersionDto updated = versionService.updateVersion(versionId, req, "test@example.com");

        assertEquals("Draft 1 Updated", updated.getVersionName());
        assertEquals("{\"summary\":\"new\"}", updated.getContentJson());
    }

    @Test
    void updateSuggestionStatus_changesStatus() {
        ResumeVersion version = new ResumeVersion();
        version.setUser(testUser);

        ResumeImprovementSuggestion suggestion = new ResumeImprovementSuggestion();
        UUID suggestionId = UUID.randomUUID();
        suggestion.setId(suggestionId);
        suggestion.setVersion(version);
        suggestion.setJob(testJob);
        suggestion.setCategory("WEAK_PROJECT_DESCRIPTION");
        suggestion.setOriginalText("Worked on ML");
        suggestion.setSuggestedText("Engineered classification model");
        suggestion.setIssue("Vague role");
        suggestion.setReason("Lacks metrics");
        suggestion.setExpectedBenefit("Demonstrates impact");
        suggestion.setStatus("PENDING");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(ResumeImprovementSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeSuggestionDto updated = versionService.updateSuggestionStatus(suggestionId, "ACCEPTED", "test@example.com");

        assertEquals("ACCEPTED", updated.getStatus());
    }

    @Test
    void integrateRoadmapSkill_appendsSkillAndProjectEvidence() {
        ResumeVersion version = new ResumeVersion();
        UUID versionId = UUID.randomUUID();
        version.setId(versionId);
        version.setUser(testUser);
        version.setParentResume(testResume);
        version.setVersionNumber(1);
        version.setVersionName("Tailored V1");
        version.setContentJson("{\"skills\":[\"Java\"],\"projects\":[]}");
        version.setRawText("Java engineer");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(versionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(ResumeVersion.class))).thenAnswer(inv -> inv.getArgument(0));

        IntegrateSkillRequest req = new IntegrateSkillRequest(
                "Docker",
                "Containerize microservices architecture",
                "Cloud Deployment Project"
        );

        ResumeVersionDto result = versionService.integrateRoadmapSkill(versionId, req, "test@example.com");

        assertNotNull(result);
        assertTrue(result.getContentJson().contains("Docker"));
        assertTrue(result.getContentJson().contains("Cloud Deployment Project"));
        assertTrue(result.getRawText().contains("Docker"));
    }
}
