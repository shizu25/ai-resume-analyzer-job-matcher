package com.resumematcher.matching.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class JobMatchResponseDto {
    private UUID id;
    private UUID jobId;
    private UUID resumeId;
    private String resumeFileName;
    private String jobTitle;
    private String company;
    private String location;
    private Double matchScore;
    private Double requiredSkillMatchPct;
    private Double preferredSkillMatchPct;
    private List<String> matchedRequiredSkills;
    private List<String> missingRequiredSkills;
    private List<String> matchedPreferredSkills;
    private List<String> missingPreferredSkills;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> recommendations;
    private OffsetDateTime calculatedAt;
    private OffsetDateTime updatedAt;

    public JobMatchResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public Double getRequiredSkillMatchPct() { return requiredSkillMatchPct; }
    public void setRequiredSkillMatchPct(Double requiredSkillMatchPct) { this.requiredSkillMatchPct = requiredSkillMatchPct; }

    public Double getPreferredSkillMatchPct() { return preferredSkillMatchPct; }
    public void setPreferredSkillMatchPct(Double preferredSkillMatchPct) { this.preferredSkillMatchPct = preferredSkillMatchPct; }

    public List<String> getMatchedRequiredSkills() { return matchedRequiredSkills; }
    public void setMatchedRequiredSkills(List<String> matchedRequiredSkills) { this.matchedRequiredSkills = matchedRequiredSkills; }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(List<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public List<String> getMatchedPreferredSkills() { return matchedPreferredSkills; }
    public void setMatchedPreferredSkills(List<String> matchedPreferredSkills) { this.matchedPreferredSkills = matchedPreferredSkills; }

    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(List<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
