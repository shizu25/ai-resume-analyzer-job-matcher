package com.resumematcher.simulator.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class RunSimulationRequest {

    @NotNull(message = "Base version ID is required")
    private UUID baseVersionId;

    @NotNull(message = "Improved version ID is required")
    private UUID improvedVersionId;

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    public RunSimulationRequest() {}

    public RunSimulationRequest(UUID baseVersionId, UUID improvedVersionId, UUID jobId) {
        this.baseVersionId = baseVersionId;
        this.improvedVersionId = improvedVersionId;
        this.jobId = jobId;
    }

    public UUID getBaseVersionId() { return baseVersionId; }
    public void setBaseVersionId(UUID baseVersionId) { this.baseVersionId = baseVersionId; }

    public UUID getImprovedVersionId() { return improvedVersionId; }
    public void setImprovedVersionId(UUID improvedVersionId) { this.improvedVersionId = improvedVersionId; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
}
