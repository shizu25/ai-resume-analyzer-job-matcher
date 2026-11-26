package com.resumematcher.simulator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.entity.*;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.simulator.dto.*;
import com.resumematcher.simulator.entity.ResumeImprovementSuggestion;
import com.resumematcher.simulator.entity.ResumeVersion;
import com.resumematcher.simulator.repository.ResumeImprovementSuggestionRepository;
import com.resumematcher.simulator.repository.ResumeVersionRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeVersionService {

    private final ResumeVersionRepository versionRepository;
    private final ResumeImprovementSuggestionRepository suggestionRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public ResumeVersionService(ResumeVersionRepository versionRepository,
                                ResumeImprovementSuggestionRepository suggestionRepository,
                                ResumeRepository resumeRepository,
                                JobRepository jobRepository,
                                UserRepository userRepository,
                                AiServiceClient aiServiceClient,
                                ObjectMapper objectMapper) {
        this.versionRepository = versionRepository;
        this.suggestionRepository = suggestionRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeVersionDto createVersion(UUID parentResumeId, CreateResumeVersionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Resume resume = resumeRepository.findById(parentResumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + parentResumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this resume");
        }

        Integer maxVersion = versionRepository.findMaxVersionNumberByParentResumeId(parentResumeId);
        int nextVersionNumber = (maxVersion != null ? maxVersion : 0) + 1;

        Job targetJob = null;
        if (request.getTargetJobId() != null) {
            targetJob = jobRepository.findById(request.getTargetJobId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getTargetJobId()));
        }

        String contentJson = request.getContentJson();
        String rawText = request.getRawText();

        if (contentJson == null || contentJson.isBlank()) {
            contentJson = serializeResumeToContentJson(resume);
        }

        if (rawText == null || rawText.isBlank()) {
            rawText = resume.getParsedText() != null ? resume.getParsedText() : "";
        }

        ResumeVersion version = new ResumeVersion();
        version.setUser(user);
        version.setParentResume(resume);
        version.setVersionNumber(nextVersionNumber);
        version.setVersionName(request.getVersionName());
        version.setTargetJob(targetJob);
        version.setContentJson(contentJson);
        version.setRawText(rawText);

        ResumeVersion saved = versionRepository.save(version);
        return toVersionDto(saved);
    }

    @Transactional
    public ResumeVersionDto integrateRoadmapSkill(UUID versionId, IntegrateSkillRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + versionId));

        if (!version.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this version");
        }

        Map<String, Object> contentMap;
        try {
            contentMap = objectMapper.readValue(version.getContentJson(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            contentMap = new HashMap<>();
        }

        // 1. Add skill to skills array if not already present
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) contentMap.computeIfAbsent("skills", k -> new ArrayList<String>());
        if (skills.stream().noneMatch(s -> s != null && s.equalsIgnoreCase(request.getSkillName()))) {
            skills.add(request.getSkillName());
        }

        // 2. Add hands-on mini-project / practical task to projects array as verifiable evidence
        if ((request.getSuggestedProject() != null && !request.getSuggestedProject().isBlank()) ||
            (request.getPracticalTasks() != null && !request.getPracticalTasks().isBlank())) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> projects = (List<Map<String, Object>>) contentMap.computeIfAbsent("projects", k -> new ArrayList<Map<String, Object>>());

            String projectTitle = (request.getSuggestedProject() != null && !request.getSuggestedProject().isBlank())
                    ? request.getSuggestedProject()
                    : request.getSkillName() + " Practical Implementation";

            boolean alreadyExists = projects.stream().anyMatch(p -> projectTitle.equalsIgnoreCase((String) p.get("title")));
            if (!alreadyExists) {
                Map<String, Object> newProject = new HashMap<>();
                newProject.put("title", projectTitle);
                newProject.put("description", request.getPracticalTasks() != null ? request.getPracticalTasks() : "Hands-on project implementing " + request.getSkillName() + ".");
                newProject.put("technologies", request.getSkillName());
                newProject.put("link", "");
                projects.add(newProject);
            }
        }

        try {
            version.setContentJson(objectMapper.writeValueAsString(contentMap));
        } catch (Exception ignored) {}

        // 3. Append to rawText for parser matching
        String addition = "\n[Acquired Skill]: " + request.getSkillName();
        if (request.getPracticalTasks() != null) {
            addition += " | Project Evidence: " + request.getPracticalTasks();
        }
        if (version.getRawText() != null && !version.getRawText().contains(request.getSkillName())) {
            version.setRawText(version.getRawText() + addition);
        }

        ResumeVersion saved = versionRepository.save(version);
        return toVersionDto(saved);
    }

    @Transactional
    public ResumeVersionDto updateVersion(UUID versionId, UpdateResumeVersionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + versionId));

        if (!version.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this version");
        }

        if (request.getVersionName() != null && !request.getVersionName().isBlank()) {
            version.setVersionName(request.getVersionName());
        }

        if (request.getContentJson() != null && !request.getContentJson().isBlank()) {
            version.setContentJson(request.getContentJson());
        }

        if (request.getRawText() != null) {
            version.setRawText(request.getRawText());
        }

        ResumeVersion saved = versionRepository.save(version);
        return toVersionDto(saved);
    }

    @Transactional(readOnly = true)
    public ResumeVersionDto getVersionById(UUID versionId, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + versionId));

        if (!version.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this version");
        }

        return toVersionDto(version);
    }

    @Transactional(readOnly = true)
    public List<ResumeVersionDto> listVersionsForResume(UUID parentResumeId, String userEmail) {
        User user = getUserByEmail(userEmail);

        Resume resume = resumeRepository.findById(parentResumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + parentResumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this resume");
        }

        return versionRepository.findAllByParentResumeIdOrderByVersionNumberAsc(parentResumeId).stream()
                .map(this::toVersionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeVersionDto> listUserVersions(String userEmail) {
        User user = getUserByEmail(userEmail);
        return versionRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toVersionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ResumeSuggestionDto> generateSuggestions(UUID versionId, GenerateSuggestionsRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + versionId));

        if (!version.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to generate suggestions for this version");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        Map<String, Object> contentMap;
        try {
            contentMap = objectMapper.readValue(version.getContentJson(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            contentMap = new HashMap<>();
            contentMap.put("raw_text", version.getRawText());
        }

        AiSuggestionRequest aiRequest = new AiSuggestionRequest(
                contentMap,
                job.getTitle(),
                job.getDescription(),
                job.getRequiredSkills(),
                job.getPreferredSkills()
        );

        AiSuggestionResponse aiResponse = aiServiceClient.generateSuggestions(aiRequest);

        // Remove old suggestions for this version & job before creating fresh ones
        List<ResumeImprovementSuggestion> existing = suggestionRepository.findAllByVersionIdAndJobIdOrderByCreatedAtDesc(version.getId(), job.getId());
        if (!existing.isEmpty()) {
            suggestionRepository.deleteAll(existing);
        }

        List<ResumeImprovementSuggestion> suggestionsToSave = new ArrayList<>();
        if (aiResponse.getSuggestions() != null) {
            for (AiSuggestionDto s : aiResponse.getSuggestions()) {
                ResumeImprovementSuggestion suggestion = new ResumeImprovementSuggestion();
                suggestion.setVersion(version);
                suggestion.setJob(job);
                suggestion.setCategory(s.getCategory());
                suggestion.setOriginalText(s.getOriginalText());
                suggestion.setSuggestedText(s.getSuggestedText());
                suggestion.setIssue(s.getIssue());
                suggestion.setReason(s.getReason());
                suggestion.setRelatedRequirement(s.getRelatedRequirement());
                suggestion.setExpectedBenefit(s.getExpectedBenefit());
                suggestion.setStatus("PENDING");
                suggestionsToSave.add(suggestion);
            }
        }

        List<ResumeImprovementSuggestion> saved = suggestionRepository.saveAll(suggestionsToSave);
        return saved.stream().map(this::toSuggestionDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeSuggestionDto> getSuggestions(UUID versionId, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + versionId));

        if (!version.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view suggestions for this version");
        }

        return suggestionRepository.findAllByVersionIdOrderByCreatedAtDesc(versionId).stream()
                .map(this::toSuggestionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResumeSuggestionDto updateSuggestionStatus(UUID suggestionId, String status, String userEmail) {
        User user = getUserByEmail(userEmail);

        ResumeImprovementSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with ID: " + suggestionId));

        if (!suggestion.getVersion().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this suggestion");
        }

        suggestion.setStatus(status.toUpperCase());
        ResumeImprovementSuggestion saved = suggestionRepository.save(suggestion);
        return toSuggestionDto(saved);
    }

    private String serializeResumeToContentJson(Resume resume) {
        Map<String, Object> map = new HashMap<>();
        map.put("candidate_name", resume.getCandidateName());
        map.put("email", resume.getCandidateEmail());
        map.put("phone", resume.getCandidatePhone());
        map.put("summary", resume.getSummary());
        map.put("raw_text", resume.getParsedText());

        List<String> skills = resume.getSkills().stream().map(ResumeSkill::getName).collect(Collectors.toList());
        map.put("skills", skills);

        List<Map<String, Object>> experiences = resume.getExperiences().stream().map(e -> {
            Map<String, Object> em = new HashMap<>();
            em.put("company", e.getCompany());
            em.put("position", e.getPosition());
            em.put("location", e.getLocation());
            em.put("start_date", e.getStartDate());
            em.put("end_date", e.getEndDate());
            em.put("description", e.getDescription());
            return em;
        }).collect(Collectors.toList());
        map.put("experience", experiences);

        List<Map<String, Object>> projects = resume.getProjects().stream().map(p -> {
            Map<String, Object> pm = new HashMap<>();
            pm.put("title", p.getTitle());
            pm.put("description", p.getDescription());
            pm.put("technologies", p.getTechnologies());
            pm.put("link", p.getLink());
            return pm;
        }).collect(Collectors.toList());
        map.put("projects", projects);

        List<Map<String, Object>> education = resume.getEducationList().stream().map(ed -> {
            Map<String, Object> edm = new HashMap<>();
            edm.put("institution", ed.getInstitution());
            edm.put("degree", ed.getDegree());
            edm.put("field_of_study", ed.getFieldOfStudy());
            edm.put("start_year", ed.getStartYear());
            edm.put("end_year", ed.getEndYear());
            return edm;
        }).collect(Collectors.toList());
        map.put("education", education);

        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public ResumeVersionDto toVersionDto(ResumeVersion version) {
        ResumeVersionDto dto = new ResumeVersionDto();
        dto.setId(version.getId());
        dto.setUserId(version.getUser().getId());
        dto.setParentResumeId(version.getParentResume().getId());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setVersionName(version.getVersionName());
        if (version.getTargetJob() != null) {
            dto.setTargetJobId(version.getTargetJob().getId());
            dto.setTargetJobTitle(version.getTargetJob().getTitle());
        }
        dto.setContentJson(version.getContentJson());
        dto.setRawText(version.getRawText());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setUpdatedAt(version.getUpdatedAt());
        dto.setSuggestionCount(version.getSuggestions() != null ? version.getSuggestions().size() : 0);
        return dto;
    }

    public ResumeSuggestionDto toSuggestionDto(ResumeImprovementSuggestion s) {
        ResumeSuggestionDto dto = new ResumeSuggestionDto();
        dto.setId(s.getId());
        dto.setVersionId(s.getVersion().getId());
        dto.setJobId(s.getJob().getId());
        dto.setJobTitle(s.getJob().getTitle());
        dto.setCategory(s.getCategory());
        dto.setOriginalText(s.getOriginalText());
        dto.setSuggestedText(s.getSuggestedText());
        dto.setIssue(s.getIssue());
        dto.setReason(s.getReason());
        dto.setRelatedRequirement(s.getRelatedRequirement());
        dto.setExpectedBenefit(s.getExpectedBenefit());
        dto.setStatus(s.getStatus());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
