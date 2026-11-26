package com.resumematcher.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class CreateResumeVersionRequest {

    @NotBlank(message = "Version name is required")
    private String versionName;

    private UUID targetJobId;

    private String contentJson;

    private String rawText;

    public CreateResumeVersionRequest() {}

    public CreateResumeVersionRequest(String versionName, UUID targetJobId, String contentJson, String rawText) {
        this.versionName = versionName;
        this.targetJobId = targetJobId;
        this.contentJson = contentJson;
        this.rawText = rawText;
    }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public UUID getTargetJobId() { return targetJobId; }
    public void setTargetJobId(UUID targetJobId) { this.targetJobId = targetJobId; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
