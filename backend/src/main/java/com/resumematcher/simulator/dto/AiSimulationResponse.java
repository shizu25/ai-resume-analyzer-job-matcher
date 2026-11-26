package com.resumematcher.simulator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSimulationResponse {

    @JsonProperty("base_score")
    private Double baseScore;

    @JsonProperty("improved_score")
    private Double improvedScore;

    @JsonProperty("score_delta")
    private Double scoreDelta;

    @JsonProperty("skill_coverage_base")
    private Double skillCoverageBase;

    @JsonProperty("skill_coverage_improved")
    private Double skillCoverageImproved;

    @JsonProperty("project_relevance_base")
    private Double projectRelevanceBase;

    @JsonProperty("project_relevance_improved")
    private Double projectRelevanceImproved;

    private String explanation;

    @JsonProperty("positive_changes")
    private List<String> positiveChanges;

    private List<String> regressions;

    public AiSimulationResponse() {}

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
}
