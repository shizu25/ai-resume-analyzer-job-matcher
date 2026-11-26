package com.resumematcher.matching;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.matching.dto.JobMatchResponseDto;
import com.resumematcher.matching.service.JobMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class JobMatchController {

    private final JobMatchingService jobMatchingService;

    public JobMatchController(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService;
    }

    @PostMapping("/jobs/{jobId}/match")
    public ResponseEntity<ApiResponse<JobMatchResponseDto>> matchJob(
            @PathVariable("jobId") UUID jobId,
            @RequestParam(value = "resumeId", required = false) UUID resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobMatchResponseDto match = jobMatchingService.matchResumeToJob(jobId, resumeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job match calculated successfully", match));
    }

    @GetMapping("/jobs/{jobId}/match")
    public ResponseEntity<ApiResponse<JobMatchResponseDto>> getMatchForJob(
            @PathVariable("jobId") UUID jobId,
            @RequestParam(value = "resumeId", required = false) UUID resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobMatchResponseDto match = jobMatchingService.getMatchForJob(jobId, resumeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(match));
    }

    @PostMapping("/matches/run")
    public ResponseEntity<ApiResponse<List<JobMatchResponseDto>>> runAllMatches(
            @RequestParam(value = "resumeId", required = false) UUID resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobMatchResponseDto> matches = jobMatchingService.runMatchesForResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job matches calculated successfully", matches));
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<JobMatchResponseDto>>> listMatches(
            @RequestParam(value = "resumeId", required = false) UUID resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobMatchResponseDto> matches = jobMatchingService.getMatchesForResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<ApiResponse<JobMatchResponseDto>> getMatchById(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobMatchResponseDto match = jobMatchingService.getMatchById(matchId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(match));
    }
}
