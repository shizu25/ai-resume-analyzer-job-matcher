package com.resumematcher.simulator.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ResumeVersionDto {

    private UUID id;
    private UUID userId;
    private UUID parentResumeId;
    private Integer versionNumber;
    private String versionName;
    private UUID targetJobId;
    private String targetJobTitle;
    private String contentJson;
    private String rawText;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int suggestionCount;

    public ResumeVersionDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getParentResumeId() { return parentResumeId; }
    public void setParentResumeId(UUID parentResumeId) { this.parentResumeId = parentResumeId; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public UUID getTargetJobId() { return targetJobId; }
    public void setTargetJobId(UUID targetJobId) { this.targetJobId = targetJobId; }

    public String getTargetJobTitle() { return targetJobTitle; }
    public void setTargetJobTitle(String targetJobTitle) { this.targetJobTitle = targetJobTitle; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getSuggestionCount() { return suggestionCount; }
    public void setSuggestionCount(int suggestionCount) { this.suggestionCount = suggestionCount; }
}
