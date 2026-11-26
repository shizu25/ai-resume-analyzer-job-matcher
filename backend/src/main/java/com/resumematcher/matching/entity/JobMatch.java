package com.resumematcher.matching.entity;

import com.resumematcher.common.StringListJsonConverter;
import com.resumematcher.job.entity.Job;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_matches")
public class JobMatch {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    @Column(name = "required_skill_match_pct", nullable = false)
    private Double requiredSkillMatchPct;

    @Column(name = "preferred_skill_match_pct", nullable = false)
    private Double preferredSkillMatchPct = 0.0;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "matched_skills", nullable = false, columnDefinition = "TEXT")
    private List<String> matchedSkills = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "missing_skills", nullable = false, columnDefinition = "TEXT")
    private List<String> missingSkills = new ArrayList<>();

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "recommendations", nullable = false, columnDefinition = "TEXT")
    private List<String> recommendations = new ArrayList<>();

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public JobMatch() {}

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (preferredSkillMatchPct == null) {
            preferredSkillMatchPct = 0.0;
        }
        if (matchedSkills == null) {
            matchedSkills = new ArrayList<>();
        }
        if (missingSkills == null) {
            missingSkills = new ArrayList<>();
        }
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }
        if (calculatedAt == null) {
            calculatedAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public Double getRequiredSkillMatchPct() { return requiredSkillMatchPct; }
    public void setRequiredSkillMatchPct(Double requiredSkillMatchPct) { this.requiredSkillMatchPct = requiredSkillMatchPct; }

    public Double getPreferredSkillMatchPct() { return preferredSkillMatchPct; }
    public void setPreferredSkillMatchPct(Double preferredSkillMatchPct) { this.preferredSkillMatchPct = preferredSkillMatchPct; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
