package com.resumematcher.roadmap.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class RoadmapItemDto {

    private UUID id;
    private String skillName;
    private String status;
    private String priority;
    private String priorityReason;
    private String whyRequired;
    private List<String> learningObjectives;
    private List<String> learningSequence;
    private String practicalTasks;
    private String suggestedProject;
    private String estimatedDuration;
    private String completionCriteria;
    private String progressStatus;
    private OffsetDateTime completedAt;

    public RoadmapItemDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public String getWhyRequired() { return whyRequired; }
    public void setWhyRequired(String whyRequired) { this.whyRequired = whyRequired; }

    public List<String> getLearningObjectives() { return learningObjectives; }
    public void setLearningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; }

    public List<String> getLearningSequence() { return learningSequence; }
    public void setLearningSequence(List<String> learningSequence) { this.learningSequence = learningSequence; }

    public String getPracticalTasks() { return practicalTasks; }
    public void setPracticalTasks(String practicalTasks) { this.practicalTasks = practicalTasks; }

    public String getSuggestedProject() { return suggestedProject; }
    public void setSuggestedProject(String suggestedProject) { this.suggestedProject = suggestedProject; }

    public String getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getCompletionCriteria() { return completionCriteria; }
    public void setCompletionCriteria(String completionCriteria) { this.completionCriteria = completionCriteria; }

    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
