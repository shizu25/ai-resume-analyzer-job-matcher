package com.resumematcher.roadmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRoadmapResponse {

    @JsonProperty("overall_coverage_pct")
    private Double overallCoveragePct;

    @JsonProperty("demonstrated_skills_count")
    private Integer demonstratedSkillsCount;

    @JsonProperty("mentioned_skills_count")
    private Integer mentionedSkillsCount;

    @JsonProperty("missing_skills_count")
    private Integer missingSkillsCount;

    @JsonProperty("related_skills_count")
    private Integer relatedSkillsCount;

    @JsonProperty("demonstrated_skills")
    private List<String> demonstratedSkills;

    @JsonProperty("mentioned_skills")
    private List<String> mentionedSkills;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    @JsonProperty("related_skills")
    private List<String> relatedSkills;

    @JsonProperty("items")
    private List<AiRoadmapItemDto> items;

    public AiRoadmapResponse() {}

    public Double getOverallCoveragePct() { return overallCoveragePct; }
    public void setOverallCoveragePct(Double overallCoveragePct) { this.overallCoveragePct = overallCoveragePct; }

    public Integer getDemonstratedSkillsCount() { return demonstratedSkillsCount; }
    public void setDemonstratedSkillsCount(Integer count) { this.demonstratedSkillsCount = count; }

    public Integer getMentionedSkillsCount() { return mentionedSkillsCount; }
    public void setMentionedSkillsCount(Integer count) { this.mentionedSkillsCount = count; }

    public Integer getMissingSkillsCount() { return missingSkillsCount; }
    public void setMissingSkillsCount(Integer count) { this.missingSkillsCount = count; }

    public Integer getRelatedSkillsCount() { return relatedSkillsCount; }
    public void setRelatedSkillsCount(Integer count) { this.relatedSkillsCount = count; }

    public List<String> getDemonstratedSkills() { return demonstratedSkills; }
    public void setDemonstratedSkills(List<String> list) { this.demonstratedSkills = list; }

    public List<String> getMentionedSkills() { return mentionedSkills; }
    public void setMentionedSkills(List<String> list) { this.mentionedSkills = list; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> list) { this.missingSkills = list; }

    public List<String> getRelatedSkills() { return relatedSkills; }
    public void setRelatedSkills(List<String> list) { this.relatedSkills = list; }

    public List<AiRoadmapItemDto> getItems() { return items; }
    public void setItems(List<AiRoadmapItemDto> items) { this.items = items; }
}
