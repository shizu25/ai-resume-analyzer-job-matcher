package com.resumematcher.simulator.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ResumeSuggestionDto {

    private UUID id;
    private UUID versionId;
    private UUID jobId;
    private String jobTitle;
    private String category;
    private String originalText;
    private String suggestedText;
    private String issue;
    private String reason;
    private String relatedRequirement;
    private String expectedBenefit;
    private String status;
    private OffsetDateTime createdAt;

    public ResumeSuggestionDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getVersionId() { return versionId; }
    public void setVersionId(UUID versionId) { this.versionId = versionId; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getSuggestedText() { return suggestedText; }
    public void setSuggestedText(String suggestedText) { this.suggestedText = suggestedText; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRelatedRequirement() { return relatedRequirement; }
    public void setRelatedRequirement(String relatedRequirement) { this.relatedRequirement = relatedRequirement; }

    public String getExpectedBenefit() { return expectedBenefit; }
    public void setExpectedBenefit(String expectedBenefit) { this.expectedBenefit = expectedBenefit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
