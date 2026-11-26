package com.resumematcher.roadmap.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoadmapResponseDto {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String company;
    private UUID resumeId;
    private String resumeFileName;
    private Double overallCoveragePct;
    private Integer demonstratedSkillsCount;
    private Integer mentionedSkillsCount;
    private Integer missingSkillsCount;
    private Integer completedTasksCount;
    private Integer totalTasksCount;
    private List<RoadmapItemDto> items = new ArrayList<>();
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public RoadmapResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public Double getOverallCoveragePct() { return overallCoveragePct; }
    public void setOverallCoveragePct(Double overallCoveragePct) { this.overallCoveragePct = overallCoveragePct; }

    public Integer getDemonstratedSkillsCount() { return demonstratedSkillsCount; }
    public void setDemonstratedSkillsCount(Integer demonstratedSkillsCount) { this.demonstratedSkillsCount = demonstratedSkillsCount; }

    public Integer getMentionedSkillsCount() { return mentionedSkillsCount; }
    public void setMentionedSkillsCount(Integer mentionedSkillsCount) { this.mentionedSkillsCount = mentionedSkillsCount; }

    public Integer getMissingSkillsCount() { return missingSkillsCount; }
    public void setMissingSkillsCount(Integer missingSkillsCount) { this.missingSkillsCount = missingSkillsCount; }

    public Integer getCompletedTasksCount() { return completedTasksCount; }
    public void setCompletedTasksCount(Integer completedTasksCount) { this.completedTasksCount = completedTasksCount; }

    public Integer getTotalTasksCount() { return totalTasksCount; }
    public void setTotalTasksCount(Integer totalTasksCount) { this.totalTasksCount = totalTasksCount; }

    public List<RoadmapItemDto> getItems() { return items; }
    public void setItems(List<RoadmapItemDto> items) { this.items = items; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
