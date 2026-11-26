package com.resumematcher.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.common.GlobalExceptionHandler;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.config.SecurityConfig;
import com.resumematcher.job.dto.CreateJobRequest;
import com.resumematcher.job.dto.JobResponseDto;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;
import com.resumematcher.job.service.JobService;
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

@WebMvcTest(controllers = JobController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void listJobsWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void listJobsWithCombinedFiltersPassesParamsToService() throws Exception {
        JobResponseDto dto = sampleDto();
        when(jobService.listJobs("Java", "Spring Boot", "Bengaluru", WorkMode.HYBRID, JobType.FULL_TIME, ExperienceLevel.SENIOR, "alice@example.com"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs")
                        .param("q", "Java")
                        .param("skill", "Spring Boot")
                        .param("location", "Bengaluru")
                        .param("workMode", "HYBRID")
                        .param("jobType", "FULL_TIME")
                        .param("experienceLevel", "SENIOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Senior Java Backend Engineer"))
                .andExpect(jsonPath("$.data[0].salaryCurrency").value("INR"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void listJobsWithInvalidEnumFilterReturns400() throws Exception {
        mockMvc.perform(get("/api/jobs").param("workMode", "INVALID_MODE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void createJobValidationFailureReturns400() throws Exception {
        CreateJobRequest invalid = new CreateJobRequest();
        invalid.setTitle("");
        invalid.setRequiredSkills(List.of());

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void createJobValidRequestReturns201() throws Exception {
        JobResponseDto dto = sampleDto();
        when(jobService.createJob(any(CreateJobRequest.class), eq("alice@example.com"))).thenReturn(dto);

        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Senior Java Backend Engineer");
        req.setCompany("Nimbus Cloud Systems");
        req.setLocation("Bengaluru, Karnataka");
        req.setWorkMode(WorkMode.HYBRID);
        req.setJobType(JobType.FULL_TIME);
        req.setExperienceLevel(ExperienceLevel.SENIOR);
        req.setSalaryMin(2200000);
        req.setSalaryMax(3200000);
        req.setSalaryCurrency("INR");
        req.setSalaryPeriod(SalaryPeriod.ANNUAL);
        req.setDescription("Build scalable APIs.");
        req.setRequiredSkills(List.of("Java", "Spring Boot"));

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Senior Java Backend Engineer"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void getMissingJobReturns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.getJobById(jobId, "alice@example.com"))
                .thenThrow(new ResourceNotFoundException("Active job not found"));

        mockMvc.perform(get("/api/jobs/" + jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "bob@example.com")
    void deleteUnownedJobReturns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        doThrow(new AccessDeniedException("Forbidden"))
                .when(jobService).deactivateJob(jobId, "bob@example.com");

        mockMvc.perform(delete("/api/jobs/" + jobId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void deleteOwnedJobReturns200() throws Exception {
        UUID jobId = UUID.randomUUID();

        mockMvc.perform(delete("/api/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(jobService).deactivateJob(jobId, "alice@example.com");
    }

    private JobResponseDto sampleDto() {
        JobResponseDto dto = new JobResponseDto();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Senior Java Backend Engineer");
        dto.setCompany("Nimbus Cloud Systems");
        dto.setLocation("Bengaluru, Karnataka");
        dto.setWorkMode(WorkMode.HYBRID);
        dto.setJobType(JobType.FULL_TIME);
        dto.setExperienceLevel(ExperienceLevel.SENIOR);
        dto.setSalaryMin(2200000);
        dto.setSalaryMax(3200000);
        dto.setSalaryCurrency("INR");
        dto.setSalaryPeriod(SalaryPeriod.ANNUAL);
        dto.setDescription("Build scalable APIs.");
        dto.setRequiredSkills(List.of("Java", "Spring Boot"));
        dto.setPreferredSkills(List.of("Docker"));
        dto.setIsActive(true);
        dto.setCanEdit(true);
        return dto;
    }
}
