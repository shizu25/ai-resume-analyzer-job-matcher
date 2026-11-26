package com.resumematcher.simulator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.simulator.dto.AiSimulationRequest;
import com.resumematcher.simulator.dto.AiSimulationResponse;
import com.resumematcher.simulator.dto.ResumeSimulationDto;
import com.resumematcher.simulator.dto.RunSimulationRequest;
import com.resumematcher.simulator.entity.ResumeSimulation;
import com.resumematcher.simulator.entity.ResumeVersion;
import com.resumematcher.simulator.repository.ResumeSimulationRepository;
import com.resumematcher.simulator.repository.ResumeVersionRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeSimulationService {

    private final ResumeSimulationRepository simulationRepository;
    private final ResumeVersionRepository versionRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public ResumeSimulationService(ResumeSimulationRepository simulationRepository,
                                  ResumeVersionRepository versionRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  AiServiceClient aiServiceClient,
                                  ObjectMapper objectMapper) {
        this.simulationRepository = simulationRepository;
        this.versionRepository = versionRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeSimulationDto runSimulation(RunSimulationRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion baseVersion = versionRepository.findById(request.getBaseVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Base version not found with ID: " + request.getBaseVersionId()));

        if (!baseVersion.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access the base resume version");
        }

        ResumeVersion improvedVersion = versionRepository.findById(request.getImprovedVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Improved version not found with ID: " + request.getImprovedVersionId()));

        if (!improvedVersion.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access the improved resume version");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        Map<String, Object> baseContent = parseContentJson(baseVersion.getContentJson(), baseVersion.getRawText());
        Map<String, Object> improvedContent = parseContentJson(improvedVersion.getContentJson(), improvedVersion.getRawText());

        AiSimulationRequest aiRequest = new AiSimulationRequest(
                baseContent,
                improvedContent,
                job.getTitle(),
                job.getDescription(),
                job.getRequiredSkills(),
                job.getPreferredSkills()
        );

        AiSimulationResponse aiResponse = aiServiceClient.simulateComparison(aiRequest);

        ResumeSimulation sim = new ResumeSimulation();
        sim.setUser(user);
        sim.setBaseVersion(baseVersion);
        sim.setImprovedVersion(improvedVersion);
        sim.setJob(job);
        sim.setBaseScore(aiResponse.getBaseScore() != null ? aiResponse.getBaseScore() : 0.0);
        sim.setImprovedScore(aiResponse.getImprovedScore() != null ? aiResponse.getImprovedScore() : 0.0);
        sim.setScoreDelta(aiResponse.getScoreDelta() != null ? aiResponse.getScoreDelta() : 0.0);
        sim.setSkillCoverageBase(aiResponse.getSkillCoverageBase() != null ? aiResponse.getSkillCoverageBase() : 0.0);
        sim.setSkillCoverageImproved(aiResponse.getSkillCoverageImproved() != null ? aiResponse.getSkillCoverageImproved() : 0.0);
        sim.setProjectRelevanceBase(aiResponse.getProjectRelevanceBase() != null ? aiResponse.getProjectRelevanceBase() : 0.0);
        sim.setProjectRelevanceImproved(aiResponse.getProjectRelevanceImproved() != null ? aiResponse.getProjectRelevanceImproved() : 0.0);
        sim.setExplanation(aiResponse.getExplanation() != null ? aiResponse.getExplanation() : "");
        sim.setPositiveChanges(aiResponse.getPositiveChanges() != null ? aiResponse.getPositiveChanges() : new ArrayList<>());
        sim.setRegressions(aiResponse.getRegressions() != null ? aiResponse.getRegressions() : new ArrayList<>());

        ResumeSimulation saved = simulationRepository.save(sim);
        return toSimulationDto(saved);
    }

    @Transactional(readOnly = true)
    public ResumeSimulationDto getSimulationById(UUID simulationId, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeSimulation sim = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulation not found with ID: " + simulationId));

        if (!sim.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this simulation");
        }

        return toSimulationDto(sim);
    }

    @Transactional(readOnly = true)
    public List<ResumeSimulationDto> listUserSimulations(String userEmail) {
        User user = getUserByEmail(userEmail);
        return simulationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toSimulationDto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> parseContentJson(String contentJson, String rawText) {
        if (contentJson != null && !contentJson.isBlank()) {
            try {
                return objectMapper.readValue(contentJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {}
        }
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("raw_text", rawText != null ? rawText : "");
        return fallback;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public ResumeSimulationDto toSimulationDto(ResumeSimulation s) {
        ResumeSimulationDto dto = new ResumeSimulationDto();
        dto.setId(s.getId());
        dto.setUserId(s.getUser().getId());
        dto.setBaseVersionId(s.getBaseVersion().getId());
        dto.setBaseVersionName(s.getBaseVersion().getVersionName());
        dto.setImprovedVersionId(s.getImprovedVersion().getId());
        dto.setImprovedVersionName(s.getImprovedVersion().getVersionName());
        dto.setJobId(s.getJob().getId());
        dto.setJobTitle(s.getJob().getTitle());
        dto.setBaseScore(s.getBaseScore());
        dto.setImprovedScore(s.getImprovedScore());
        dto.setScoreDelta(s.getScoreDelta());
        dto.setSkillCoverageBase(s.getSkillCoverageBase());
        dto.setSkillCoverageImproved(s.getSkillCoverageImproved());
        dto.setProjectRelevanceBase(s.getProjectRelevanceBase());
        dto.setProjectRelevanceImproved(s.getProjectRelevanceImproved());
        dto.setExplanation(s.getExplanation());
        dto.setPositiveChanges(s.getPositiveChanges());
        dto.setRegressions(s.getRegressions());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
