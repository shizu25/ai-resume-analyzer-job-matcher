package com.resumematcher.roadmap.service;

import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.entity.ResumeExperience;
import com.resumematcher.resume.entity.ResumeProject;
import com.resumematcher.resume.entity.ResumeSkill;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.service.AiServiceClient;
import com.resumematcher.roadmap.dto.*;
import com.resumematcher.roadmap.entity.RoadmapItem;
import com.resumematcher.roadmap.entity.SkillRoadmap;
import com.resumematcher.roadmap.repository.RoadmapItemRepository;
import com.resumematcher.roadmap.repository.SkillRoadmapRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    private final SkillRoadmapRepository roadmapRepository;
    private final RoadmapItemRepository itemRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;

    public RoadmapService(SkillRoadmapRepository roadmapRepository,
                          RoadmapItemRepository itemRepository,
                          ResumeRepository resumeRepository,
                          JobRepository jobRepository,
                          UserRepository userRepository,
                          AiServiceClient aiServiceClient) {
        this.roadmapRepository = roadmapRepository;
        this.itemRepository = itemRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public RoadmapResponseDto generateOrGetRoadmap(GenerateRoadmapRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + request.getResumeId()));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this resume");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        // Check if an existing roadmap is already cached
        return roadmapRepository.findByUserIdAndJobIdAndResumeId(user.getId(), job.getId(), resume.getId())
                .map(this::toResponseDto)
                .orElseGet(() -> createNewRoadmap(user, resume, job));
    }

    @Transactional
    public RoadmapResponseDto regenerateRoadmap(GenerateRoadmapRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + request.getResumeId()));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this resume");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        // Delete existing if present to regenerate clean roadmap
        roadmapRepository.findByUserIdAndJobIdAndResumeId(user.getId(), job.getId(), resume.getId())
                .ifPresent(roadmapRepository::delete);

        return createNewRoadmap(user, resume, job);
    }

    private RoadmapResponseDto createNewRoadmap(User user, Resume resume, Job job) {
        List<String> resumeSkills = resume.getSkills().stream()
                .map(ResumeSkill::getName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        List<String> experiences = resume.getExperiences().stream()
                .map(ResumeExperience::getDescription)
                .filter(d -> d != null && !d.isBlank())
                .collect(Collectors.toList());

        List<String> projects = resume.getProjects().stream()
                .map(ResumeProject::getDescription)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toList());

        AiRoadmapRequest aiRequest = new AiRoadmapRequest();
        aiRequest.setResumeSkills(resumeSkills);
        aiRequest.setResumeText(resume.getParsedText() != null ? resume.getParsedText() : "");
        aiRequest.setExperienceDescriptions(experiences);
        aiRequest.setProjectDescriptions(projects);
        aiRequest.setJobTitle(job.getTitle());
        aiRequest.setJobDescription(job.getDescription());
        aiRequest.setRequiredSkills(job.getRequiredSkills());
        aiRequest.setPreferredSkills(job.getPreferredSkills());
        aiRequest.setExperienceLevel(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : "MID");

        AiRoadmapResponse aiResponse = aiServiceClient.generateRoadmap(aiRequest);

        SkillRoadmap roadmap = new SkillRoadmap();
        roadmap.setUser(user);
        roadmap.setResume(resume);
        roadmap.setJob(job);
        roadmap.setOverallCoveragePct(aiResponse.getOverallCoveragePct() != null ? aiResponse.getOverallCoveragePct() : 0.0);
        roadmap.setDemonstratedSkillsCount(aiResponse.getDemonstratedSkillsCount() != null ? aiResponse.getDemonstratedSkillsCount() : 0);
        roadmap.setMentionedSkillsCount(aiResponse.getMentionedSkillsCount() != null ? aiResponse.getMentionedSkillsCount() : 0);
        roadmap.setMissingSkillsCount(aiResponse.getMissingSkillsCount() != null ? aiResponse.getMissingSkillsCount() : 0);

        if (aiResponse.getItems() != null) {
            for (AiRoadmapItemDto itemDto : aiResponse.getItems()) {
                RoadmapItem item = new RoadmapItem();
                item.setRoadmap(roadmap);
                item.setSkillName(itemDto.getSkillName());
                item.setStatus(itemDto.getStatus());
                item.setPriority(itemDto.getPriority());
                item.setPriorityReason(itemDto.getPriorityReason());
                item.setWhyRequired(itemDto.getWhyRequired());
                item.setLearningObjectives(itemDto.getLearningObjectives() != null ? itemDto.getLearningObjectives() : new ArrayList<>());
                item.setLearningSequence(itemDto.getLearningSequence() != null ? itemDto.getLearningSequence() : new ArrayList<>());
                item.setPracticalTasks(itemDto.getPracticalTasks());
                item.setSuggestedProject(itemDto.getSuggestedProject());
                item.setEstimatedDuration(itemDto.getEstimatedDuration());
                item.setCompletionCriteria(itemDto.getCompletionCriteria());
                item.setProgressStatus("NOT_STARTED");
                roadmap.getItems().add(item);
            }
        }

        SkillRoadmap saved = roadmapRepository.save(roadmap);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public RoadmapResponseDto getRoadmapById(UUID roadmapId, String userEmail) {
        User user = getUserByEmail(userEmail);
        SkillRoadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        if (!roadmap.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this roadmap");
        }

        return toResponseDto(roadmap);
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponseDto> listRoadmaps(String userEmail) {
        User user = getUserByEmail(userEmail);
        return roadmapRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoadmapItemDto updateItemProgress(UUID roadmapId, UUID itemId, String progressStatus, String userEmail) {
        User user = getUserByEmail(userEmail);
        SkillRoadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + roadmapId));

        if (!roadmap.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this roadmap");
        }

        RoadmapItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap item not found with ID: " + itemId));

        if (!item.getRoadmap().getId().equals(roadmap.getId())) {
            throw new IllegalArgumentException("Item does not belong to the specified roadmap");
        }

        item.setProgressStatus(progressStatus);
        if ("COMPLETED".equalsIgnoreCase(progressStatus)) {
            item.setCompletedAt(OffsetDateTime.now());
        } else {
            item.setCompletedAt(null);
        }

        RoadmapItem saved = itemRepository.save(item);
        return toItemDto(saved);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public RoadmapResponseDto toResponseDto(SkillRoadmap roadmap) {
        RoadmapResponseDto dto = new RoadmapResponseDto();
        dto.setId(roadmap.getId());
        dto.setJobId(roadmap.getJob().getId());
        dto.setJobTitle(roadmap.getJob().getTitle());
        dto.setCompany(roadmap.getJob().getCompany());
        dto.setResumeId(roadmap.getResume().getId());
        dto.setResumeFileName(roadmap.getResume().getFileName());
        dto.setOverallCoveragePct(roadmap.getOverallCoveragePct());
        dto.setDemonstratedSkillsCount(roadmap.getDemonstratedSkillsCount());
        dto.setMentionedSkillsCount(roadmap.getMentionedSkillsCount());
        dto.setMissingSkillsCount(roadmap.getMissingSkillsCount());
        dto.setCreatedAt(roadmap.getCreatedAt());
        dto.setUpdatedAt(roadmap.getUpdatedAt());

        List<RoadmapItemDto> itemDtos = roadmap.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        int total = itemDtos.size();
        long completed = itemDtos.stream().filter(i -> "COMPLETED".equalsIgnoreCase(i.getProgressStatus())).count();
        dto.setTotalTasksCount(total);
        dto.setCompletedTasksCount((int) completed);

        return dto;
    }

    public RoadmapItemDto toItemDto(RoadmapItem item) {
        RoadmapItemDto dto = new RoadmapItemDto();
        dto.setId(item.getId());
        dto.setSkillName(item.getSkillName());
        dto.setStatus(item.getStatus());
        dto.setPriority(item.getPriority());
        dto.setPriorityReason(item.getPriorityReason());
        dto.setWhyRequired(item.getWhyRequired());
        dto.setLearningObjectives(item.getLearningObjectives());
        dto.setLearningSequence(item.getLearningSequence());
        dto.setPracticalTasks(item.getPracticalTasks());
        dto.setSuggestedProject(item.getSuggestedProject());
        dto.setEstimatedDuration(item.getEstimatedDuration());
        dto.setCompletionCriteria(item.getCompletionCriteria());
        dto.setProgressStatus(item.getProgressStatus());
        dto.setCompletedAt(item.getCompletedAt());
        return dto;
    }
}
