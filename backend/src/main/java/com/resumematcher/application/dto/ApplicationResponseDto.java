package com.resumematcher.application.dto;

import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ApplicationResponseDto {
    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String company;
    private String location;
    private WorkMode workMode;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryCurrency;
    private SalaryPeriod salaryPeriod;
    private Boolean jobActive;
    private UUID resumeId;
    private String resumeFileName;
    private ApplicationStatus status;
    private Double matchScoreSnapshot;
    private String notes;
    private OffsetDateTime appliedAt;
    private OffsetDateTime updatedAt;

    public ApplicationResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }

    public SalaryPeriod getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(SalaryPeriod salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    public Boolean getJobActive() { return jobActive; }
    public void setJobActive(Boolean jobActive) { this.jobActive = jobActive; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

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
