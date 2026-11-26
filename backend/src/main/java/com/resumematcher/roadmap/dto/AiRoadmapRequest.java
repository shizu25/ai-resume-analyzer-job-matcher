package com.resumematcher.roadmap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AiRoadmapRequest {

    @JsonProperty("resume_skills")
    private List<String> resumeSkills;

    @JsonProperty("resume_text")
    private String resumeText;

    @JsonProperty("experience_descriptions")
    private List<String> experienceDescriptions;

    @JsonProperty("project_descriptions")
    private List<String> projectDescriptions;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_description")
    private String jobDescription;

    @JsonProperty("required_skills")
    private List<String> requiredSkills;

    @JsonProperty("preferred_skills")
    private List<String> preferredSkills;

    @JsonProperty("experience_level")
    private String experienceLevel;

    public AiRoadmapRequest() {}

    public List<String> getResumeSkills() { return resumeSkills; }
    public void setResumeSkills(List<String> resumeSkills) { this.resumeSkills = resumeSkills; }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public List<String> getExperienceDescriptions() { return experienceDescriptions; }
    public void setExperienceDescriptions(List<String> experienceDescriptions) { this.experienceDescriptions = experienceDescriptions; }

    public List<String> getProjectDescriptions() { return projectDescriptions; }
    public void setProjectDescriptions(List<String> projectDescriptions) { this.projectDescriptions = projectDescriptions; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
}
