package com.resumematcher.application;

import com.resumematcher.application.dto.ApplicationResponseDto;
import com.resumematcher.application.dto.CreateApplicationRequest;
import com.resumematcher.application.dto.UpdateApplicationRequest;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.service.ApplicationService;
import com.resumematcher.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> applyToJob(
            @Valid @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponseDto created = applicationService.applyToJob(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> listApplications(
            @RequestParam(value = "status", required = false) ApplicationStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ApplicationResponseDto> applications = applicationService.listApplications(status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> getApplication(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponseDto application = applicationService.getApplicationById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateApplication(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponseDto updated = applicationService.updateApplication(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Application updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        applicationService.deleteApplication(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Application removed successfully", null));
    }
}
