package com.resumematcher.roadmap.controller;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.roadmap.dto.GenerateRoadmapRequest;
import com.resumematcher.roadmap.dto.RoadmapItemDto;
import com.resumematcher.roadmap.dto.RoadmapResponseDto;
import com.resumematcher.roadmap.dto.UpdateRoadmapProgressRequest;
import com.resumematcher.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> generateRoadmap(
            @Valid @RequestBody GenerateRoadmapRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoadmapResponseDto roadmap = roadmapService.generateOrGetRoadmap(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill-gap roadmap generated successfully", roadmap));
    }

    @PostMapping("/regenerate")
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> regenerateRoadmap(
            @Valid @RequestBody GenerateRoadmapRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoadmapResponseDto roadmap = roadmapService.regenerateRoadmap(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Skill-gap roadmap regenerated successfully", roadmap));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoadmapResponseDto>>> listRoadmaps(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<RoadmapResponseDto> list = roadmapService.listRoadmaps(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> getRoadmap(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoadmapResponseDto roadmap = roadmapService.getRoadmapById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(roadmap));
    }

    @PatchMapping("/{roadmapId}/items/{itemId}/progress")
    public ResponseEntity<ApiResponse<RoadmapItemDto>> updateProgress(
            @PathVariable("roadmapId") UUID roadmapId,
            @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody UpdateRoadmapProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RoadmapItemDto updated = roadmapService.updateItemProgress(roadmapId, itemId, request.getProgressStatus(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Roadmap item progress updated", updated));
    }
}
