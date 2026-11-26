package com.resumematcher.job.dto;

import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company cannot exceed 255 characters")
    private String company;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotNull(message = "Work mode is required")
    private WorkMode workMode;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @Min(value = 0, message = "Minimum salary must be non-negative")
    private Integer salaryMin;

    @Min(value = 0, message = "Maximum salary must be non-negative")
    private Integer salaryMax;

    @Size(max = 10, message = "Currency code cannot exceed 10 characters")
    private String salaryCurrency;

    private SalaryPeriod salaryPeriod;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotEmpty(message = "At least one required skill must be provided")
    private List<@NotBlank(message = "Skill name cannot be blank") String> requiredSkills;

    private List<@NotBlank(message = "Preferred skill name cannot be blank") String> preferredSkills;

    @Size(max = 500, message = "Application URL cannot exceed 500 characters")
    private String applicationUrl;

    public CreateJobRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }
}
