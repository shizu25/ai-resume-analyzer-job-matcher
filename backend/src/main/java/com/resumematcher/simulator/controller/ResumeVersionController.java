package com.resumematcher.simulator.controller;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.simulator.dto.*;
import com.resumematcher.simulator.service.ResumeVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ResumeVersionController {

    private final ResumeVersionService versionService;

    public ResumeVersionController(ResumeVersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping("/resumes/{resumeId}/versions")
    public ResponseEntity<ApiResponse<ResumeVersionDto>> createVersion(
            @PathVariable("resumeId") UUID resumeId,
            @Valid @RequestBody CreateResumeVersionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto created = versionService.createVersion(resumeId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume version created successfully", created));
    }

    @GetMapping("/resumes/{resumeId}/versions")
    public ResponseEntity<ApiResponse<List<ResumeVersionDto>>> listVersionsForResume(
            @PathVariable("resumeId") UUID resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeVersionDto> list = versionService.listVersionsForResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/resume-versions")
    public ResponseEntity<ApiResponse<List<ResumeVersionDto>>> listAllUserVersions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeVersionDto> list = versionService.listUserVersions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/resume-versions/{versionId}")
    public ResponseEntity<ApiResponse<ResumeVersionDto>> getVersion(
            @PathVariable("versionId") UUID versionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto version = versionService.getVersionById(versionId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(version));
    }

    @PutMapping("/resume-versions/{versionId}")
    public ResponseEntity<ApiResponse<ResumeVersionDto>> updateVersion(
            @PathVariable("versionId") UUID versionId,
            @RequestBody UpdateResumeVersionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto updated = versionService.updateVersion(versionId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume version updated successfully", updated));
    }

    @PostMapping("/resume-versions/{versionId}/integrate-skill")
    public ResponseEntity<ApiResponse<ResumeVersionDto>> integrateSkill(
            @PathVariable("versionId") UUID versionId,
            @Valid @RequestBody IntegrateSkillRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto updated = versionService.integrateRoadmapSkill(versionId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Completed roadmap skill and project evidence integrated successfully", updated));
    }

    @PostMapping("/resume-versions/{versionId}/suggestions")
    public ResponseEntity<ApiResponse<List<ResumeSuggestionDto>>> generateSuggestions(
            @PathVariable("versionId") UUID versionId,
            @Valid @RequestBody GenerateSuggestionsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeSuggestionDto> suggestions = versionService.generateSuggestions(versionId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Improvement suggestions generated", suggestions));
    }

    @GetMapping("/resume-versions/{versionId}/suggestions")
    public ResponseEntity<ApiResponse<List<ResumeSuggestionDto>>> getSuggestions(
            @PathVariable("versionId") UUID versionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeSuggestionDto> suggestions = versionService.getSuggestions(versionId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @PatchMapping("/resume-suggestions/{suggestionId}/status")
    public ResponseEntity<ApiResponse<ResumeSuggestionDto>> updateSuggestionStatus(
            @PathVariable("suggestionId") UUID suggestionId,
            @Valid @RequestBody UpdateSuggestionStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeSuggestionDto updated = versionService.updateSuggestionStatus(suggestionId, request.getStatus(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Suggestion status updated", updated));
    }
}
