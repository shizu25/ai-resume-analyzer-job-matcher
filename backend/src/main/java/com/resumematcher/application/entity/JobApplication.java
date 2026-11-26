package com.resumematcher.application.entity;

import com.resumematcher.job.entity.Job;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

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
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "match_score_snapshot")
    private Double matchScoreSnapshot;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public JobApplication() {}

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }
        if (appliedAt == null) {
            appliedAt = OffsetDateTime.now();
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

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Double getMatchScoreSnapshot() { return matchScoreSnapshot; }
    public void setMatchScoreSnapshot(Double matchScoreSnapshot) { this.matchScoreSnapshot = matchScoreSnapshot; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(OffsetDateTime appliedAt) { this.appliedAt = appliedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
