package com.resumematcher.roadmap.entity;

import com.resumematcher.job.entity.Job;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "skill_roadmaps", uniqueConstraints = {
        @UniqueConstraint(name = "uq_skill_roadmaps_user_job_resume", columnNames = {"user_id", "job_id", "resume_id"})
})
public class SkillRoadmap {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "overall_coverage_pct", nullable = false)
    private Double overallCoveragePct;

    @Column(name = "demonstrated_skills_count", nullable = false)
    private Integer demonstratedSkillsCount = 0;

    @Column(name = "mentioned_skills_count", nullable = false)
    private Integer mentionedSkillsCount = 0;

    @Column(name = "missing_skills_count", nullable = false)
    private Integer missingSkillsCount = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoadmapItem> items = new ArrayList<>();

    public SkillRoadmap() {}

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public Double getOverallCoveragePct() { return overallCoveragePct; }
    public void setOverallCoveragePct(Double overallCoveragePct) { this.overallCoveragePct = overallCoveragePct; }

    public Integer getDemonstratedSkillsCount() { return demonstratedSkillsCount; }
    public void setDemonstratedSkillsCount(Integer count) { this.demonstratedSkillsCount = count; }

    public Integer getMentionedSkillsCount() { return mentionedSkillsCount; }
    public void setMentionedSkillsCount(Integer count) { this.mentionedSkillsCount = count; }

    public Integer getMissingSkillsCount() { return missingSkillsCount; }
    public void setMissingSkillsCount(Integer count) { this.missingSkillsCount = count; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<RoadmapItem> getItems() { return items; }
    public void setItems(List<RoadmapItem> items) { this.items = items; }
}
