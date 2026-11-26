package com.resumematcher.roadmap;

import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.roadmap.dto.*;
import com.resumematcher.roadmap.entity.RoadmapItem;
import com.resumematcher.roadmap.entity.SkillRoadmap;
import com.resumematcher.roadmap.repository.RoadmapItemRepository;
import com.resumematcher.roadmap.repository.SkillRoadmapRepository;
import com.resumematcher.roadmap.service.RoadmapService;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock
    private SkillRoadmapRepository roadmapRepository;

    @Mock
    private RoadmapItemRepository itemRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    private RoadmapService roadmapService;

    private User testUser;
    private Resume testResume;
    private Job testJob;
    private UUID userId;
    private UUID resumeId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        roadmapService = new RoadmapService(
                roadmapRepository, itemRepository, resumeRepository, jobRepository, userRepository, aiServiceClient
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
        testResume.setFileName("test_resume.pdf");
        testResume.setParsedText("Experienced Python and Java developer");

        testJob = new Job();
        testJob.setId(jobId);
        testJob.setTitle("Backend Engineer");
        testJob.setCompany("Acme Corp");
        testJob.setDescription("Looking for strong Python and Docker skills");
        testJob.setRequiredSkills(List.of("Python", "Docker"));
        testJob.setPreferredSkills(List.of("Kubernetes"));
        testJob.setExperienceLevel(ExperienceLevel.MID);
    }

    @Test
    void generateOrGetRoadmap_returnsExisting_whenCached() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(testResume));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        SkillRoadmap existing = new SkillRoadmap();
        existing.setId(UUID.randomUUID());
        existing.setUser(testUser);
        existing.setResume(testResume);
        existing.setJob(testJob);
        existing.setOverallCoveragePct(75.0);
        existing.setDemonstratedSkillsCount(1);
        existing.setMissingSkillsCount(1);

        when(roadmapRepository.findByUserIdAndJobIdAndResumeId(userId, jobId, resumeId))
                .thenReturn(Optional.of(existing));

        GenerateRoadmapRequest req = new GenerateRoadmapRequest(jobId, resumeId);
        RoadmapResponseDto result = roadmapService.generateOrGetRoadmap(req, "test@example.com");

        assertNotNull(result);
        assertEquals(75.0, result.getOverallCoveragePct());
        verify(aiServiceClient, never()).generateRoadmap(any());
    }

    @Test
    void generateOrGetRoadmap_callsAiService_whenNew() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(testResume));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(roadmapRepository.findByUserIdAndJobIdAndResumeId(userId, jobId, resumeId))
                .thenReturn(Optional.empty());

        AiRoadmapResponse aiResp = new AiRoadmapResponse();
        aiResp.setOverallCoveragePct(50.0);
        aiResp.setDemonstratedSkillsCount(1);
        aiResp.setMissingSkillsCount(1);
        AiRoadmapItemDto itemDto = new AiRoadmapItemDto();
        itemDto.setSkillName("Docker");
        itemDto.setStatus("MISSING");
        itemDto.setPriority("HIGH");
        itemDto.setPriorityReason("Mandatory containerization tool");
        itemDto.setWhyRequired("Production deployment");
        itemDto.setPracticalTasks("Dockerize a Spring Boot app");
        itemDto.setSuggestedProject("Multi-container compose setup");
        itemDto.setEstimatedDuration("1-2 weeks");
        itemDto.setCompletionCriteria("Explain container lifecycle");
        aiResp.setItems(List.of(itemDto));

        when(aiServiceClient.generateRoadmap(any())).thenReturn(aiResp);
        when(roadmapRepository.save(any(SkillRoadmap.class))).thenAnswer(inv -> {
            SkillRoadmap r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        GenerateRoadmapRequest req = new GenerateRoadmapRequest(jobId, resumeId);
        RoadmapResponseDto result = roadmapService.generateOrGetRoadmap(req, "test@example.com");

        assertNotNull(result);
        assertEquals(50.0, result.getOverallCoveragePct());
        assertEquals(1, result.getItems().size());
        assertEquals("Docker", result.getItems().get(0).getSkillName());
        verify(aiServiceClient, times(1)).generateRoadmap(any());
        verify(roadmapRepository, times(1)).save(any());
    }

    @Test
    void generateOrGetRoadmap_throwsAccessDenied_whenResumeBelongsToOtherUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        testResume.setUser(otherUser);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(testResume));

        GenerateRoadmapRequest req = new GenerateRoadmapRequest(jobId, resumeId);
        assertThrows(AccessDeniedException.class, () -> roadmapService.generateOrGetRoadmap(req, "test@example.com"));
    }

    @Test
    void updateItemProgress_updatesStatusAndCompletedAt() {
        SkillRoadmap roadmap = new SkillRoadmap();
        UUID roadmapId = UUID.randomUUID();
        roadmap.setId(roadmapId);
        roadmap.setUser(testUser);

        RoadmapItem item = new RoadmapItem();
        UUID itemId = UUID.randomUUID();
        item.setId(itemId);
        item.setRoadmap(roadmap);
        item.setSkillName("Docker");
        item.setProgressStatus("NOT_STARTED");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(roadmapRepository.findById(roadmapId)).thenReturn(Optional.of(roadmap));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(RoadmapItem.class))).thenAnswer(inv -> inv.getArgument(0));

        RoadmapItemDto updated = roadmapService.updateItemProgress(roadmapId, itemId, "COMPLETED", "test@example.com");

        assertEquals("COMPLETED", updated.getProgressStatus());
        assertNotNull(updated.getCompletedAt());
    }
}
