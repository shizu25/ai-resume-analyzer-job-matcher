package com.resumematcher.matching;

import com.resumematcher.common.GlobalExceptionHandler;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.config.SecurityConfig;
import com.resumematcher.matching.dto.JobMatchResponseDto;
import com.resumematcher.matching.service.JobMatchingService;
import com.resumematcher.security.AuthEntryPointJwt;
import com.resumematcher.security.CustomUserDetailsService;
import com.resumematcher.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobMatchController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class JobMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobMatchingService jobMatchingService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void endpointsWithoutAuthReturn401() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/jobs/" + UUID.randomUUID() + "/match"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void matchJobReturns200WithGranularSkills() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobMatchResponseDto dto = sampleMatchDto(jobId);
        when(jobMatchingService.matchResumeToJob(jobId, null, "alice@example.com")).thenReturn(dto);

        mockMvc.perform(post("/api/jobs/" + jobId + "/match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matchScore").value(74.7))
                .andExpect(jsonPath("$.data.requiredSkillMatchPct").value(83.3))
                .andExpect(jsonPath("$.data.preferredSkillMatchPct").value(40.0))
                .andExpect(jsonPath("$.data.matchedRequiredSkills[0]").value("Java"))
                .andExpect(jsonPath("$.data.missingRequiredSkills[0]").value("Microservices"))
                .andExpect(jsonPath("$.data.matchedPreferredSkills[0]").value("Redis"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void matchJobWithoutActiveResumeReturns400() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobMatchingService.matchResumeToJob(jobId, null, "alice@example.com"))
                .thenThrow(new IllegalArgumentException("No active resume found."));

        mockMvc.perform(post("/api/jobs/" + jobId + "/match"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No active resume found."));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void matchJobWithOtherUsersResumeReturns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID resumeId = UUID.randomUUID();
        when(jobMatchingService.matchResumeToJob(jobId, resumeId, "alice@example.com"))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/match").param("resumeId", resumeId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void getMatchForMissingJobReturns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobMatchingService.getMatchForJob(jobId, null, "alice@example.com"))
                .thenThrow(new ResourceNotFoundException("No match result found"));

        mockMvc.perform(get("/api/jobs/" + jobId + "/match"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void runAllMatchesAndListMatchesReturn200() throws Exception {
        JobMatchResponseDto dto = sampleMatchDto(UUID.randomUUID());
        when(jobMatchingService.runMatchesForResume(null, "alice@example.com")).thenReturn(List.of(dto));
        when(jobMatchingService.getMatchesForResume(null, "alice@example.com")).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/matches/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].matchScore").value(74.7));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].jobTitle").value("Senior Java Backend Engineer"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void getMatchByIdEnforcesOwnership() throws Exception {
        UUID matchId = UUID.randomUUID();
        when(jobMatchingService.getMatchById(matchId, "alice@example.com"))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/api/matches/" + matchId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private JobMatchResponseDto sampleMatchDto(UUID jobId) {
        JobMatchResponseDto dto = new JobMatchResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setJobId(jobId);
        dto.setResumeId(UUID.randomUUID());
        dto.setResumeFileName("Jane_Doe_Resume.pdf");
        dto.setJobTitle("Senior Java Backend Engineer");
        dto.setCompany("Nimbus Cloud Systems");
        dto.setLocation("Bengaluru, Karnataka");
        dto.setMatchScore(74.7);
        dto.setRequiredSkillMatchPct(83.3);
        dto.setPreferredSkillMatchPct(40.0);
        dto.setMatchedRequiredSkills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "REST API"));
        dto.setMissingRequiredSkills(List.of("Microservices"));
        dto.setMatchedPreferredSkills(List.of("Redis", "AWS"));
        dto.setMissingPreferredSkills(List.of("Kubernetes", "Kafka", "Flyway"));
        dto.setMatchedSkills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "REST API", "Redis", "AWS"));
        dto.setMissingSkills(List.of("Microservices", "Kubernetes", "Kafka", "Flyway"));
        dto.setRecommendations(List.of("Core skill gap: Add or demonstrate 'Microservices'."));
        return dto;
    }
}
