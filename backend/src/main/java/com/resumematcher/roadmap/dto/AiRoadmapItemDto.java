package com.resumematcher.roadmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRoadmapItemDto {

    @JsonProperty("skill_name")
    private String skillName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("priority_reason")
    private String priorityReason;

    @JsonProperty("why_required")
    private String whyRequired;

    @JsonProperty("learning_objectives")
    private List<String> learningObjectives;

    @JsonProperty("learning_sequence")
    private List<String> learningSequence;

    @JsonProperty("practical_tasks")
    private String practicalTasks;

    @JsonProperty("suggested_project")
    private String suggestedProject;

    @JsonProperty("estimated_duration")
    private String estimatedDuration;

    @JsonProperty("completion_criteria")
    private String completionCriteria;

    @JsonProperty("progress_status")
    private String progressStatus;

    public AiRoadmapItemDto() {}

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
}
