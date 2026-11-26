package com.resumematcher.simulator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSuggestionResponse {

    private List<AiSuggestionDto> suggestions;

    public AiSuggestionResponse() {}

    public List<AiSuggestionDto> getSuggestions() { return suggestions; }
    public void setSuggestions(List<AiSuggestionDto> suggestions) { this.suggestions = suggestions; }
}
