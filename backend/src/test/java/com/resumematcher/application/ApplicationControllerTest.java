package com.resumematcher.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.application.dto.ApplicationResponseDto;
import com.resumematcher.application.dto.CreateApplicationRequest;
import com.resumematcher.application.dto.UpdateApplicationRequest;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.service.ApplicationService;
import com.resumematcher.common.ConflictException;
import com.resumematcher.common.GlobalExceptionHandler;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.config.SecurityConfig;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;
import com.resumematcher.security.AuthEntryPointJwt;
import com.resumematcher.security.CustomUserDetailsService;
import com.resumematcher.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApplicationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void endpointsWithoutAuthReturn401() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void applyToJobReturns201WithJobDetailsAndSnapshot() throws Exception {
        UUID jobId = UUID.randomUUID();
        ApplicationResponseDto dto = sampleDto(jobId, ApplicationStatus.APPLIED);
        when(applicationService.applyToJob(any(CreateApplicationRequest.class), eq("alice@example.com")))
                .thenReturn(dto);

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(jobId);
        req.setNotes("Applied online");

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.matchScoreSnapshot").value(74.7))
                .andExpect(jsonPath("$.data.jobTitle").value("Senior Java Backend Engineer"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void applyWithoutJobIdReturns400() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void duplicateApplicationReturns409() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(applicationService.applyToJob(any(CreateApplicationRequest.class), eq("alice@example.com")))
                .thenThrow(new ConflictException("You have already applied to this job"));

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(jobId);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You have already applied to this job"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void listApplicationsSupportsStatusFilterAndRejectsInvalidStatus() throws Exception {
        ApplicationResponseDto dto = sampleDto(UUID.randomUUID(), ApplicationStatus.INTERVIEWING);
        when(applicationService.listApplications(ApplicationStatus.INTERVIEWING, "alice@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/applications").param("status", "INTERVIEWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("INTERVIEWING"));

        mockMvc.perform(get("/api/applications").param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void updateApplicationStatusAndNotesReturns200() throws Exception {
        UUID appId = UUID.randomUUID();
        ApplicationResponseDto dto = sampleDto(UUID.randomUUID(), ApplicationStatus.OFFER);
        dto.setId(appId);
        dto.setNotes("Offer received");

        when(applicationService.updateApplication(eq(appId), any(UpdateApplicationRequest.class), eq("alice@example.com")))
                .thenReturn(dto);

        UpdateApplicationRequest req = new UpdateApplicationRequest();
        req.setStatus(ApplicationStatus.OFFER);
        req.setNotes("Offer received");

        mockMvc.perform(patch("/api/applications/" + appId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("OFFER"))
                .andExpect(jsonPath("$.data.notes").value("Offer received"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void updateApplicationWithInvalidEnumReturns400() throws Exception {
        mockMvc.perform(patch("/api/applications/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void accessOtherUsersApplicationReturns403AndMissingReturns404() throws Exception {
        UUID forbiddenId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        when(applicationService.getApplicationById(forbiddenId, "alice@example.com"))
                .thenThrow(new AccessDeniedException("Forbidden"));
        doThrow(new ResourceNotFoundException("Application not found"))
                .when(applicationService).deleteApplication(missingId, "alice@example.com");

        mockMvc.perform(get("/api/applications/" + forbiddenId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(delete("/api/applications/" + missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void deleteOwnedApplicationReturns200() throws Exception {
        UUID appId = UUID.randomUUID();

        mockMvc.perform(delete("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(applicationService).deleteApplication(appId, "alice@example.com");
    }

    private ApplicationResponseDto sampleDto(UUID jobId, ApplicationStatus status) {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setJobId(jobId);
        dto.setJobTitle("Senior Java Backend Engineer");
        dto.setCompany("Nimbus Cloud Systems");
        dto.setLocation("Bengaluru, Karnataka");
        dto.setWorkMode(WorkMode.HYBRID);
        dto.setJobType(JobType.FULL_TIME);
        dto.setExperienceLevel(ExperienceLevel.SENIOR);
        dto.setSalaryMin(2200000);
        dto.setSalaryMax(3200000);
        dto.setSalaryCurrency("INR");
        dto.setSalaryPeriod(SalaryPeriod.ANNUAL);
        dto.setJobActive(true);
        dto.setResumeId(UUID.randomUUID());
        dto.setResumeFileName("Jane_Resume.pdf");
        dto.setStatus(status);
        dto.setMatchScoreSnapshot(74.7);
        dto.setNotes("Applied online");
        return dto;
    }
}
