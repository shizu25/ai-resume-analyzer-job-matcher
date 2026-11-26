package com.resumematcher.analytics.dto;

import java.util.UUID;

public class TopMatchedRoleDto {
    private UUID jobId;
    private String jobTitle;
    private String company;
    private String location;
    private Double matchScore;

    public TopMatchedRoleDto() {}

    public TopMatchedRoleDto(UUID jobId, String jobTitle, String company, String location, Double matchScore) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.matchScore = matchScore;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
}
