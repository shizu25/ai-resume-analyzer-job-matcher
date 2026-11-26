package com.resumematcher.simulator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSuggestionDto {

    private String category;

    @JsonProperty("original_text")
    private String originalText;

    @JsonProperty("suggested_text")
    private String suggestedText;

    private String issue;
    private String reason;

    @JsonProperty("related_requirement")
    private String relatedRequirement;

    @JsonProperty("expected_benefit")
    private String expectedBenefit;

    public AiSuggestionDto() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getSuggestedText() { return suggestedText; }
    public void setSuggestedText(String suggestedText) { this.suggestedText = suggestedText; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRelatedRequirement() { return relatedRequirement; }
    public void setRelatedRequirement(String relatedRequirement) { this.relatedRequirement = relatedRequirement; }

    public String getExpectedBenefit() { return expectedBenefit; }
    public void setExpectedBenefit(String expectedBenefit) { this.expectedBenefit = expectedBenefit; }
}
