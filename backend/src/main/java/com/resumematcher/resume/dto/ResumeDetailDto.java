package com.resumematcher.resume.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ResumeDetailDto {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Boolean isActive;
    private String parsingStatus;
    private String parsedText;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String summary;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<SkillDto> skills;
    private List<EducationDto> education;
    private List<ExperienceDto> experience;
    private List<ProjectDto> projects;
    private List<SuggestionDto> suggestions;
    private List<String> warnings;

    public ResumeDetailDto() {}

    public static class SkillDto {
        private UUID id;
        private String name;
        private String category;
        public SkillDto() {}
        public SkillDto(UUID id, String name, String category) {
            this.id = id; this.name = name; this.category = category;
        }
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class EducationDto {
        private UUID id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private String grade;
        public EducationDto() {}
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getFieldOfStudy() { return fieldOfStudy; }
        public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }
        public Integer getStartYear() { return startYear; }
        public void setStartYear(Integer startYear) { this.startYear = startYear; }
        public Integer getEndYear() { return endYear; }
        public void setEndYear(Integer endYear) { this.endYear = endYear; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }

    public static class ExperienceDto {
        private UUID id;
        private String company;
        private String position;
        private String location;
        private String startDate;
        private String endDate;
        private Boolean isCurrent;
        private String description;
        public ExperienceDto() {}
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Boolean getIsCurrent() { return isCurrent; }
        public void setIsCurrent(Boolean current) { isCurrent = current; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ProjectDto {
        private UUID id;
        private String title;
        private String description;
        private String technologies;
        private String link;
        public ProjectDto() {}
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTechnologies() { return technologies; }
        public void setTechnologies(String technologies) { this.technologies = technologies; }
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }

    public static class SuggestionDto {
        private UUID id;
        private String category;
        private String priority;
        private String issue;
        private String recommendation;
        public SuggestionDto() {}
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getIssue() { return issue; }
        public void setIssue(String issue) { this.issue = issue; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public String getParsingStatus() { return parsingStatus; }
    public void setParsingStatus(String parsingStatus) { this.parsingStatus = parsingStatus; }
    public String getParsedText() { return parsedText; }
    public void setParsedText(String parsedText) { this.parsedText = parsedText; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
    public String getCandidatePhone() { return candidatePhone; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }
    public List<EducationDto> getEducation() { return education; }
    public void setEducation(List<EducationDto> education) { this.education = education; }
    public List<ExperienceDto> getExperiences() { return experience; }
    public void setExperiences(List<ExperienceDto> experience) { this.experience = experience; }
    public List<ProjectDto> getProjects() { return projects; }
    public void setProjects(List<ProjectDto> projects) { this.projects = projects; }
    public List<SuggestionDto> getSuggestions() { return suggestions; }
    public void setSuggestions(List<SuggestionDto> suggestions) { this.suggestions = suggestions; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
