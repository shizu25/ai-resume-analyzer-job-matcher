package com.resumematcher.simulator.entity;

import com.resumematcher.job.entity.Job;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resume_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_resume_versions_parent_number", columnNames = {"parent_resume_id", "version_number"})
})
public class ResumeVersion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_resume_id", nullable = false)
    private Resume parentResume;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_name", nullable = false, length = 255)
    private String versionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_job_id")
    private Job targetJob;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeImprovementSuggestion> suggestions = new ArrayList<>();

    public ResumeVersion() {}

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

    public Resume getParentResume() { return parentResume; }
    public void setParentResume(Resume parentResume) { this.parentResume = parentResume; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public Job getTargetJob() { return targetJob; }
    public void setTargetJob(Job targetJob) { this.targetJob = targetJob; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ResumeImprovementSuggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<ResumeImprovementSuggestion> suggestions) { this.suggestions = suggestions; }
}
