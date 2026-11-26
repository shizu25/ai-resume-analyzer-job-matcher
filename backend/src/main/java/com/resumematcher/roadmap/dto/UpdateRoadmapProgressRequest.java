package com.resumematcher.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateRoadmapProgressRequest {

    @NotBlank(message = "Progress status is required")
    @Pattern(regexp = "^(NOT_STARTED|IN_PROGRESS|COMPLETED)$", message = "Status must be NOT_STARTED, IN_PROGRESS, or COMPLETED")
    private String progressStatus;

    public UpdateRoadmapProgressRequest() {}

    public UpdateRoadmapProgressRequest(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
}
