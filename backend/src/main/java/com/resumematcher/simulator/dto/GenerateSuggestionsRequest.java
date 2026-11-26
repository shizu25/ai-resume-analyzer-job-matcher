package com.resumematcher.simulator.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class GenerateSuggestionsRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    public GenerateSuggestionsRequest() {}

    public GenerateSuggestionsRequest(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
}
