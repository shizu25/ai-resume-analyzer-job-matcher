package com.resumematcher.simulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AiSimulationRequest {

    @JsonProperty("version_a_content")
    private Map<String, Object> versionAContent;

    @JsonProperty("version_b_content")
    private Map<String, Object> versionBContent;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_description")
    private String jobDescription;

    @JsonProperty("required_skills")
    private List<String> requiredSkills;

    @JsonProperty("preferred_skills")
    private List<String> preferredSkills;

    public AiSimulationRequest() {}

    public AiSimulationRequest(Map<String, Object> versionAContent, Map<String, Object> versionBContent,
                               String jobTitle, String jobDescription,
                               List<String> requiredSkills, List<String> preferredSkills) {
        this.versionAContent = versionAContent;
        this.versionBContent = versionBContent;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
    }

    public Map<String, Object> getVersionAContent() { return versionAContent; }
    public void setVersionAContent(Map<String, Object> versionAContent) { this.versionAContent = versionAContent; }

    public Map<String, Object> getVersionBContent() { return versionBContent; }
    public void setVersionBContent(Map<String, Object> versionBContent) { this.versionBContent = versionBContent; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }
}
