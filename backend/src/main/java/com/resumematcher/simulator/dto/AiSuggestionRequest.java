package com.resumematcher.simulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AiSuggestionRequest {

    @JsonProperty("resume_content")
    private Map<String, Object> resumeContent;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_description")
    private String jobDescription;

    @JsonProperty("required_skills")
    private List<String> requiredSkills;

    @JsonProperty("preferred_skills")
    private List<String> preferredSkills;

    public AiSuggestionRequest() {}

    public AiSuggestionRequest(Map<String, Object> resumeContent, String jobTitle, String jobDescription,
                               List<String> requiredSkills, List<String> preferredSkills) {
        this.resumeContent = resumeContent;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
    }

    public Map<String, Object> getResumeContent() { return resumeContent; }
    public void setResumeContent(Map<String, Object> resumeContent) { this.resumeContent = resumeContent; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }
}
