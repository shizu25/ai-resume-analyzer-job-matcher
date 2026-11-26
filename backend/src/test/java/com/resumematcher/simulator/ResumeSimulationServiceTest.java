package com.resumematcher.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.simulator.dto.AiSimulationResponse;
import com.resumematcher.simulator.dto.ResumeSimulationDto;
import com.resumematcher.simulator.dto.RunSimulationRequest;
import com.resumematcher.simulator.entity.ResumeSimulation;
import com.resumematcher.simulator.entity.ResumeVersion;
import com.resumematcher.simulator.repository.ResumeSimulationRepository;
import com.resumematcher.simulator.repository.ResumeVersionRepository;
import com.resumematcher.simulator.service.ResumeSimulationService;
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
class ResumeSimulationServiceTest {

    @Mock
    private ResumeSimulationRepository simulationRepository;

    @Mock
    private ResumeVersionRepository versionRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResumeSimulationService simulationService;

    private User testUser;
    private ResumeVersion baseVersion;
    private ResumeVersion improvedVersion;
    private Job testJob;
    private UUID userId;
    private UUID baseId;
    private UUID improvedId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        simulationService = new ResumeSimulationService(
                simulationRepository, versionRepository, jobRepository,
                userRepository, aiServiceClient, objectMapper
        );

        userId = UUID.randomUUID();
        baseId = UUID.randomUUID();
        improvedId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        baseVersion = new ResumeVersion();
        baseVersion.setId(baseId);
        baseVersion.setUser(testUser);
        baseVersion.setVersionName("Base V1");
        baseVersion.setContentJson("{\"skills\":[\"Java\"]}");

        improvedVersion = new ResumeVersion();
        improvedVersion.setId(improvedId);
        improvedVersion.setUser(testUser);
        improvedVersion.setVersionName("Improved V2");
        improvedVersion.setContentJson("{\"skills\":[\"Java\",\"Spring Boot\"]}");

        testJob = new Job();
        testJob.setId(jobId);
        testJob.setTitle("Backend Engineer");
        testJob.setDescription("Java and Spring Boot requirements");
        testJob.setRequiredSkills(List.of("Java", "Spring Boot"));
    }

    @Test
    void runSimulation_computesDeltaAndPersists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(versionRepository.findById(baseId)).thenReturn(Optional.of(baseVersion));
        when(versionRepository.findById(improvedId)).thenReturn(Optional.of(improvedVersion));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        AiSimulationResponse aiResp = new AiSimulationResponse();
        aiResp.setBaseScore(50.0);
        aiResp.setImprovedScore(85.0);
        aiResp.setScoreDelta(35.0);
        aiResp.setSkillCoverageBase(50.0);
        aiResp.setSkillCoverageImproved(100.0);
        aiResp.setProjectRelevanceBase(40.0);
        aiResp.setProjectRelevanceImproved(80.0);
        aiResp.setExplanation("Improved version includes Spring Boot and concrete projects.");
        aiResp.setPositiveChanges(List.of("Added required skill Spring Boot (+50.0% coverage)"));
        aiResp.setRegressions(List.of());

        when(aiServiceClient.simulateComparison(any())).thenReturn(aiResp);
        when(simulationRepository.save(any(ResumeSimulation.class))).thenAnswer(inv -> {
            ResumeSimulation s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        RunSimulationRequest req = new RunSimulationRequest(baseId, improvedId, jobId);
        ResumeSimulationDto result = simulationService.runSimulation(req, "test@example.com");

        assertNotNull(result);
        assertEquals(50.0, result.getBaseScore());
        assertEquals(85.0, result.getImprovedScore());
        assertEquals(35.0, result.getScoreDelta());
        assertEquals(1, result.getPositiveChanges().size());
        verify(simulationRepository, times(1)).save(any());
    }

    @Test
    void runSimulation_throwsAccessDenied_whenUserDoesNotOwnVersion() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        improvedVersion.setUser(otherUser);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(versionRepository.findById(baseId)).thenReturn(Optional.of(baseVersion));
        when(versionRepository.findById(improvedId)).thenReturn(Optional.of(improvedVersion));

        RunSimulationRequest req = new RunSimulationRequest(baseId, improvedId, jobId);
        assertThrows(AccessDeniedException.class, () -> simulationService.runSimulation(req, "test@example.com"));
    }
}
