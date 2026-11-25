package com.resumematcher.resume.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resume_suggestions")
public class ResumeSuggestion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "priority", nullable = false)
    private String priority;

    @Column(name = "issue", columnDefinition = "TEXT", nullable = false)
    private String issue;

    @Column(name = "recommendation", columnDefinition = "TEXT", nullable = false)
    private String recommendation;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ResumeSuggestion() {}

    public ResumeSuggestion(Resume resume, String category, String priority, String issue, String recommendation) {
        this.resume = resume;
        this.category = category;
        this.priority = priority;
        this.issue = issue;
        this.recommendation = recommendation;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
