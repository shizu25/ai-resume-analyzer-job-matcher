package com.resumematcher.job;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.job.dto.CreateJobRequest;
import com.resumematcher.job.dto.JobResponseDto;
import com.resumematcher.job.dto.UpdateJobRequest;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.WorkMode;
import com.resumematcher.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponseDto>>> listJobs(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "workMode", required = false) WorkMode workMode,
            @RequestParam(value = "jobType", required = false) JobType jobType,
            @RequestParam(value = "experienceLevel", required = false) ExperienceLevel experienceLevel,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobResponseDto> jobs = jobService.listJobs(
                query, skill, location, workMode, jobType, experienceLevel, userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDto>> getJob(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponseDto job = jobService.getJobById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponseDto>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponseDto created = jobService.createJob(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDto>> updateJob(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateJobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponseDto updated = jobService.updateJob(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateJob(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deactivateJob(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job deactivated successfully", null));
    }
}
