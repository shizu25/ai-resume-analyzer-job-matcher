package com.resumematcher.resume.service;

import com.resumematcher.resume.dto.ParsedResumeAiResponse;
import com.resumematcher.roadmap.dto.AiRoadmapRequest;
import com.resumematcher.roadmap.dto.AiRoadmapResponse;
import com.resumematcher.simulator.dto.AiSimulationRequest;
import com.resumematcher.simulator.dto.AiSimulationResponse;
import com.resumematcher.simulator.dto.AiSuggestionRequest;
import com.resumematcher.simulator.dto.AiSuggestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.time.Duration;

@Service
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);
    private final WebClient aiServiceWebClient;

    public AiServiceClient(WebClient aiServiceWebClient) {
        this.aiServiceWebClient = aiServiceWebClient;
    }

    public ParsedResumeAiResponse parseResume(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Resume document file not found at: " + filePath);
        }

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(file));

        try {
            return aiServiceWebClient.post()
                    .uri("/internal/resume/parse")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(ParsedResumeAiResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (Exception e) {
            log.error("AI service parsing request failed for file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("AI service parsing failed: " + e.getMessage(), e);
        }
    }

    public AiRoadmapResponse generateRoadmap(AiRoadmapRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/internal/roadmap/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiRoadmapResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (Exception e) {
            log.error("AI service roadmap generation failed: {}", e.getMessage());
            throw new RuntimeException("AI service roadmap generation failed: " + e.getMessage(), e);
        }
    }

    public AiSuggestionResponse generateSuggestions(AiSuggestionRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/internal/simulator/suggestions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiSuggestionResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (Exception e) {
            log.error("AI service suggestions generation failed: {}", e.getMessage());
            throw new RuntimeException("AI service suggestions generation failed: " + e.getMessage(), e);
        }
    }

    public AiSimulationResponse simulateComparison(AiSimulationRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/internal/simulator/simulate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiSimulationResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (Exception e) {
            log.error("AI service simulation comparison failed: {}", e.getMessage());
            throw new RuntimeException("AI service simulation comparison failed: " + e.getMessage(), e);
        }
    }
}
