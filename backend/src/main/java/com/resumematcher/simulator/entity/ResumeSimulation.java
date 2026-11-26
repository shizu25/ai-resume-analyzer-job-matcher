package com.resumematcher.simulator.entity;

import com.resumematcher.common.StringListJsonConverter;
import com.resumematcher.job.entity.Job;
import com.resumematcher.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resume_simulations")
public class ResumeSimulation {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_version_id", nullable = false)
    private ResumeVersion baseVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "improved_version_id", nullable = false)
    private ResumeVersion improvedVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "base_score", nullable = false)
    private Double baseScore;

    @Column(name = "improved_score", nullable = false)
    private Double improvedScore;

    @Column(name = "score_delta", nullable = false)
    private Double scoreDelta;

    @Column(name = "skill_coverage_base", nullable = false)
    private Double skillCoverageBase;

    @Column(name = "skill_coverage_improved", nullable = false)
    private Double skillCoverageImproved;

    @Column(name = "project_relevance_base", nullable = false)
    private Double projectRelevanceBase;

    @Column(name = "project_relevance_improved", nullable = false)
    private Double projectRelevanceImproved;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "positive_changes", nullable = false, columnDefinition = "TEXT")
    private List<String> positiveChanges = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "regressions", nullable = false, columnDefinition = "TEXT")
    private List<String> regressions = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ResumeSimulation() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ResumeVersion getBaseVersion() { return baseVersion; }
    public void setBaseVersion(ResumeVersion baseVersion) { this.baseVersion = baseVersion; }

    public ResumeVersion getImprovedVersion() { return improvedVersion; }
    public void setImprovedVersion(ResumeVersion improvedVersion) { this.improvedVersion = improvedVersion; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

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
