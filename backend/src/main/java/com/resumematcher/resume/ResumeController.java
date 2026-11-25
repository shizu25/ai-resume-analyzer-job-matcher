package com.resumematcher.resume;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.resume.dto.ResumeDetailDto;
import com.resumematcher.resume.dto.ResumeResponseDto;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeResponseDto>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponseDto response = resumeService.uploadResume(file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponseDto>>> listResumes(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeResponseDto> list = resumeService.listResumes(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeDetailDto>> getResume(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeDetailDto details = resumeService.getResumeDetails(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Resume resume = resumeService.getResumeWithOwnership(id, userDetails.getUsername());
        Resource resource = resumeService.downloadResume(id, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getFileName() + "\"")
                .body(resource);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> setActiveResume(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponseDto response = resumeService.setActiveResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume set as default active resume", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ApiResponse<ResumeDetailDto>> analyzeResume(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeDetailDto analysis = resumeService.analyzeResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume parsed and analyzed successfully", analysis));
    }
}
