package com.resumematcher.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    private UUID resumeId;

    @Size(max = 5000, message = "Notes cannot exceed 5000 characters")
    private String notes;

    public CreateApplicationRequest() {}

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
