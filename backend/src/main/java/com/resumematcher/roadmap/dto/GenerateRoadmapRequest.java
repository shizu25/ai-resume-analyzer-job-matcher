package com.resumematcher.roadmap.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class GenerateRoadmapRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    private UUID resumeId; // Optional: defaults to active resume

    public GenerateRoadmapRequest() {}

    public GenerateRoadmapRequest(UUID jobId, UUID resumeId) {
        this.jobId = jobId;
        this.resumeId = resumeId;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }
}
