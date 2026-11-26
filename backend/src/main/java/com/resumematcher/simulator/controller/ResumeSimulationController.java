package com.resumematcher.simulator.controller;

import com.resumematcher.common.ApiResponse;
import com.resumematcher.simulator.dto.ResumeSimulationDto;
import com.resumematcher.simulator.dto.RunSimulationRequest;
import com.resumematcher.simulator.service.ResumeSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resume-simulations")
public class ResumeSimulationController {

    private final ResumeSimulationService simulationService;

    public ResumeSimulationController(ResumeSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeSimulationDto>> runSimulation(
            @Valid @RequestBody RunSimulationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeSimulationDto result = simulationService.runSimulation(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume simulation completed successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeSimulationDto>>> listSimulations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeSimulationDto> list = simulationService.listUserSimulations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeSimulationDto>> getSimulation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeSimulationDto result = simulationService.getSimulationById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
