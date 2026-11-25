package com.resumematcher.resume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeAiResponse {

    @JsonProperty("candidate_name")
    private String candidateName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("raw_text")
    private String rawText;

    @JsonProperty("skills")
    private List<ParsedSkill> skills;

    @JsonProperty("education")
    private List<ParsedEducation> education;

    @JsonProperty("experience")
    private List<ParsedExperience> experience;

    @JsonProperty("projects")
    private List<ParsedProject> projects;

    @JsonProperty("suggestions")
    private List<ParsedSuggestion> suggestions;

    @JsonProperty("warnings")
    private List<String> warnings;

    public ParsedResumeAiResponse() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedSkill {
        private String name;
        private String category;
        private Double confidence;
        private String evidence;

        public ParsedSkill() {}
        public ParsedSkill(String name, String category) {
            this.name = name; this.category = category;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedEducation {
        private String institution;
        private String degree;
        private String field;
        @JsonProperty("start_year")
        private Integer startYear;
        @JsonProperty("end_year")
        private Integer endYear;
        private String grade;
        private Double confidence;
        private String evidence;

        public ParsedEducation() {}
        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public Integer getStartYear() { return startYear; }
        public void setStartYear(Integer startYear) { this.startYear = startYear; }
        public Integer getEndYear() { return endYear; }
        public void setEndYear(Integer endYear) { this.endYear = endYear; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedExperience {
        private String company;
        private String position;
        private String location;
        @JsonProperty("start_date")
        private String startDate;
        @JsonProperty("end_date")
        private String endDate;
        @JsonProperty("is_current")
        private Boolean isCurrent;
        private String description;
        private Double confidence;
        private String evidence;

        public ParsedExperience() {}
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
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedProject {
        private String title;
        private String description;
        private String technologies;
        private String link;
        private Double confidence;
        private String evidence;

        public ParsedProject() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTechnologies() { return technologies; }
        public void setTechnologies(String technologies) { this.technologies = technologies; }
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedSuggestion {
        private String category;
        private String priority;
        private String issue;
        private String recommendation;
        public ParsedSuggestion() {}
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getIssue() { return issue; }
        public void setIssue(String issue) { this.issue = issue; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public List<ParsedSkill> getSkills() { return skills; }
    public void setSkills(List<ParsedSkill> skills) { this.skills = skills; }
    public List<ParsedEducation> getEducation() { return education; }
    public void setEducation(List<ParsedEducation> education) { this.education = education; }
    public List<ParsedExperience> getExperience() { return experience; }
    public void setExperience(List<ParsedExperience> experience) { this.experience = experience; }
    public List<ParsedProject> getProjects() { return projects; }
    public void setProjects(List<ParsedProject> projects) { this.projects = projects; }
    public List<ParsedSuggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<ParsedSuggestion> suggestions) { this.suggestions = suggestions; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
