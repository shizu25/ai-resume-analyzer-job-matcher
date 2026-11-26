package com.resumematcher.simulator.dto;

public class UpdateResumeVersionRequest {

    private String versionName;
    private String contentJson;
    private String rawText;

    public UpdateResumeVersionRequest() {}

    public UpdateResumeVersionRequest(String versionName, String contentJson, String rawText) {
        this.versionName = versionName;
        this.contentJson = contentJson;
        this.rawText = rawText;
    }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
