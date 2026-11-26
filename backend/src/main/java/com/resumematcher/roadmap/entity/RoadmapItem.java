package com.resumematcher.roadmap.entity;

import com.resumematcher.common.StringListJsonConverter;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmap_items")
public class RoadmapItem {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private SkillRoadmap roadmap;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // DEMONSTRATED, MENTIONED, MISSING, RELATED

    @Column(name = "priority", nullable = false, length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "priority_reason", nullable = false, columnDefinition = "TEXT")
    private String priorityReason;

    @Column(name = "why_required", nullable = false, columnDefinition = "TEXT")
    private String whyRequired;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "learning_objectives", nullable = false, columnDefinition = "TEXT")
    private List<String> learningObjectives = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "learning_sequence", nullable = false, columnDefinition = "TEXT")
    private List<String> learningSequence = new ArrayList<>();

    @Column(name = "practical_tasks", nullable = false, columnDefinition = "TEXT")
    private String practicalTasks;

    @Column(name = "suggested_project", nullable = false, columnDefinition = "TEXT")
    private String suggestedProject;

    @Column(name = "estimated_duration", nullable = false, length = 100)
    private String estimatedDuration;

    @Column(name = "completion_criteria", nullable = false, columnDefinition = "TEXT")
    private String completionCriteria;

    @Column(name = "progress_status", nullable = false, length = 30)
    private String progressStatus = "NOT_STARTED"; // NOT_STARTED, IN_PROGRESS, COMPLETED

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public RoadmapItem() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SkillRoadmap getRoadmap() { return roadmap; }
    public void setRoadmap(SkillRoadmap roadmap) { this.roadmap = roadmap; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
