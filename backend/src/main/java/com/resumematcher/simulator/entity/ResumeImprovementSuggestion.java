package com.resumematcher.simulator.entity;

import com.resumematcher.job.entity.Job;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resume_improvement_suggestions")
public class ResumeImprovementSuggestion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ResumeVersion version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "suggested_text", nullable = false, columnDefinition = "TEXT")
    private String suggestedText;

    @Column(name = "issue", nullable = false, columnDefinition = "TEXT")
    private String issue;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "related_requirement", length = 255)
    private String relatedRequirement;

    @Column(name = "expected_benefit", nullable = false, columnDefinition = "TEXT")
    private String expectedBenefit;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ResumeImprovementSuggestion() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ResumeVersion getVersion() { return version; }
    public void setVersion(ResumeVersion version) { this.version = version; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getSuggestedText() { return suggestedText; }
    public void setSuggestedText(String suggestedText) { this.suggestedText = suggestedText; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRelatedRequirement() { return relatedRequirement; }
    public void setRelatedRequirement(String relatedRequirement) { this.relatedRequirement = relatedRequirement; }

    public String getExpectedBenefit() { return expectedBenefit; }
    public void setExpectedBenefit(String expectedBenefit) { this.expectedBenefit = expectedBenefit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
