package com.resumematcher.simulator.dto;

import jakarta.validation.constraints.NotBlank;

public class IntegrateSkillRequest {

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private String practicalTasks;
    private String suggestedProject;

    public IntegrateSkillRequest() {}

    public IntegrateSkillRequest(String skillName, String practicalTasks, String suggestedProject) {
        this.skillName = skillName;
        this.practicalTasks = practicalTasks;
        this.suggestedProject = suggestedProject;
    }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getPracticalTasks() { return practicalTasks; }
    public void setPracticalTasks(String practicalTasks) { this.practicalTasks = practicalTasks; }

    public String getSuggestedProject() { return suggestedProject; }
    public void setSuggestedProject(String suggestedProject) { this.suggestedProject = suggestedProject; }
}
