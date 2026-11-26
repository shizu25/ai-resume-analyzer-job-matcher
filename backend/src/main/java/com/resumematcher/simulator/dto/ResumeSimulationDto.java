package com.resumematcher.simulator.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ResumeSimulationDto {

    private UUID id;
    private UUID userId;
    private UUID baseVersionId;
    private String baseVersionName;
    private UUID improvedVersionId;
    private String improvedVersionName;
    private UUID jobId;
    private String jobTitle;
    private Double baseScore;
    private Double improvedScore;
    private Double scoreDelta;
    private Double skillCoverageBase;
    private Double skillCoverageImproved;
    private Double projectRelevanceBase;
    private Double projectRelevanceImproved;
    private String explanation;
    private List<String> positiveChanges;
    private List<String> regressions;
    private OffsetDateTime createdAt;

    public ResumeSimulationDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBaseVersionId() { return baseVersionId; }
    public void setBaseVersionId(UUID baseVersionId) { this.baseVersionId = baseVersionId; }

    public String getBaseVersionName() { return baseVersionName; }
    public void setBaseVersionName(String baseVersionName) { this.baseVersionName = baseVersionName; }

    public UUID getImprovedVersionId() { return improvedVersionId; }
    public void setImprovedVersionId(UUID improvedVersionId) { this.improvedVersionId = improvedVersionId; }

    public String getImprovedVersionName() { return improvedVersionName; }
    public void setImprovedVersionName(String improvedVersionName) { this.improvedVersionName = improvedVersionName; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Double getBaseScore() { return baseScore; }
    public void setBaseScore(Double baseScore) { this.baseScore = baseScore; }

    public Double getImprovedScore() { return improvedScore; }
    public void setImprovedScore(Double improvedScore) { this.improvedScore = improvedScore; }

    public Double getScoreDelta() { return scoreDelta; }
    public void setScoreDelta(Double scoreDelta) { this.scoreDelta = scoreDelta; }

    public Double getSkillCoverageBase() { return skillCoverageBase; }
    public void setSkillCoverageBase(Double skillCoverageBase) { this.skillCoverageBase = skillCoverageBase; }

    public Double getSkillCoverageImproved() { return skillCoverageImproved; }
    public void setSkillCoverageImproved(Double skillCoverageImproved) { this.skillCoverageImproved = skillCoverageImproved; }

    public Double getProjectRelevanceBase() { return projectRelevanceBase; }
    public void setProjectRelevanceBase(Double projectRelevanceBase) { this.projectRelevanceBase = projectRelevanceBase; }

    public Double getProjectRelevanceImproved() { return projectRelevanceImproved; }
    public void setProjectRelevanceImproved(Double projectRelevanceImproved) { this.projectRelevanceImproved = projectRelevanceImproved; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getPositiveChanges() { return positiveChanges; }
    public void setPositiveChanges(List<String> positiveChanges) { this.positiveChanges = positiveChanges; }

    public List<String> getRegressions() { return regressions; }
    public void setRegressions(List<String> regressions) { this.regressions = regressions; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
