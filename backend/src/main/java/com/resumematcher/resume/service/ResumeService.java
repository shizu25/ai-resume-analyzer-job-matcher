package com.resumematcher.resume.service;

import com.resumematcher.resume.dto.ParsedResumeAiResponse;
import com.resumematcher.resume.dto.ResumeDetailDto;
import com.resumematcher.resume.dto.ResumeResponseDto;
import com.resumematcher.resume.entity.*;
import com.resumematcher.resume.repository.*;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeProjectRepository projectRepository;
    private final ResumeSuggestionRepository suggestionRepository;
    private final ResumeWarningRepository warningRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AiServiceClient aiServiceClient;

    public ResumeService(ResumeRepository resumeRepository,
                         ResumeSkillRepository skillRepository,
                         ResumeEducationRepository educationRepository,
                         ResumeExperienceRepository experienceRepository,
                         ResumeProjectRepository projectRepository,
                         ResumeSuggestionRepository suggestionRepository,
                         ResumeWarningRepository warningRepository,
                         UserRepository userRepository,
                         FileStorageService fileStorageService,
                         AiServiceClient aiServiceClient) {
        this.resumeRepository = resumeRepository;
        this.skillRepository = skillRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.suggestionRepository = suggestionRepository;
        this.warningRepository = warningRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public ResumeResponseDto uploadResume(MultipartFile file, String email) {
        User user = getUser(email);

        FileStorageService.StorageResult metadata;
        try {
            metadata = fileStorageService.store(file);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileName(metadata.originalFilename());
        resume.setStoredFileName(metadata.storedFileName());
        resume.setFilePath(metadata.filePath());
        resume.setFileType(metadata.fileType());
        resume.setFileSize(metadata.fileSize());
        resume.setParsingStatus("UPLOADED");

        boolean hasActive = resumeRepository.findByUserIdAndIsActiveTrue(user.getId()).isPresent();
        resume.setIsActive(!hasActive);

        Resume saved = resumeRepository.save(resume);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponseDto> listResumes(String email) {
        User user = getUser(email);
        return resumeRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumeDetailDto getResumeDetails(UUID resumeId, String email) {
        Resume resume = getResumeWithOwnership(resumeId, email);
        return mapToDetailDto(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDetailDto getResumeDetail(UUID resumeId, String email) {
        return getResumeDetails(resumeId, email);
    }

    @Transactional(readOnly = true)
    public Resource downloadResume(UUID resumeId, String email) {
        Resume resume = getResumeWithOwnership(resumeId, email);
        return fileStorageService.loadAsResource(resume.getFilePath());
    }

    @Transactional
    public ResumeResponseDto setActiveResume(UUID resumeId, String email) {
        User user = getUser(email);
        Resume targetResume = getResumeWithOwnership(resumeId, email);

        List<Resume> userResumes = resumeRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        for (Resume r : userResumes) {
            r.setIsActive(r.getId().equals(targetResume.getId()));
            resumeRepository.save(r);
        }

        return mapToResponseDto(targetResume);
    }

    @Transactional
    public void deleteResume(UUID resumeId, String email) {
        Resume resume = getResumeWithOwnership(resumeId, email);
        String filePath = resume.getFilePath();

        resumeRepository.delete(resume);
        fileStorageService.deleteFile(filePath);
    }

    @Transactional
    public ResumeDetailDto analyzeResume(UUID resumeId, String email) {
        Resume resume = getResumeWithOwnership(resumeId, email);

        resume.setParsingStatus("PROCESSING");
        resumeRepository.save(resume);

        try {
            ParsedResumeAiResponse aiResponse = aiServiceClient.parseResume(resume.getFilePath());

            // Clear previous extracted data to ensure idempotency
            skillRepository.deleteByResumeId(resume.getId());
            educationRepository.deleteByResumeId(resume.getId());
            experienceRepository.deleteByResumeId(resume.getId());
            projectRepository.deleteByResumeId(resume.getId());
            suggestionRepository.deleteByResumeId(resume.getId());
            warningRepository.deleteByResumeId(resume.getId());

            resume.setCandidateName(aiResponse.getCandidateName());
            resume.setCandidateEmail(aiResponse.getEmail());
            resume.setCandidatePhone(aiResponse.getPhone());
            resume.setSummary(aiResponse.getSummary());
            resume.setParsedText(aiResponse.getRawText());
            resume.setParsingStatus("COMPLETED");

            // Persist Skills
            if (aiResponse.getSkills() != null) {
                for (ParsedResumeAiResponse.ParsedSkill s : aiResponse.getSkills()) {
                    ResumeSkill skill = new ResumeSkill(resume, s.getName(), s.getCategory());
                    skillRepository.save(skill);
                }
            }

            // Persist Education
            if (aiResponse.getEducation() != null) {
                for (ParsedResumeAiResponse.ParsedEducation ed : aiResponse.getEducation()) {
                    ResumeEducation edu = new ResumeEducation();
                    edu.setResume(resume);
                    edu.setInstitution(ed.getInstitution());
                    edu.setDegree(ed.getDegree());
                    edu.setFieldOfStudy(ed.getField());
                    edu.setStartYear(ed.getStartYear());
                    edu.setEndYear(ed.getEndYear());
                    edu.setGrade(ed.getGrade());
                    educationRepository.save(edu);
                }
            }

            // Persist Experience
            if (aiResponse.getExperience() != null) {
                for (ParsedResumeAiResponse.ParsedExperience exp : aiResponse.getExperience()) {
                    ResumeExperience experience = new ResumeExperience();
                    experience.setResume(resume);
                    experience.setCompany(exp.getCompany());
                    experience.setPosition(exp.getPosition());
                    experience.setLocation(exp.getLocation());
                    experience.setStartDate(exp.getStartDate());
                    experience.setEndDate(exp.getEndDate());
                    experience.setIsCurrent(Boolean.TRUE.equals(exp.getIsCurrent()));
                    experience.setDescription(exp.getDescription());
                    experienceRepository.save(experience);
                }
            }

            // Persist Projects
            if (aiResponse.getProjects() != null) {
                for (ParsedResumeAiResponse.ParsedProject prj : aiResponse.getProjects()) {
                    ResumeProject project = new ResumeProject();
                    project.setResume(resume);
                    project.setTitle(prj.getTitle());
                    project.setDescription(prj.getDescription());
                    project.setTechnologies(prj.getTechnologies());
                    project.setLink(prj.getLink());
                    projectRepository.save(project);
                }
            }

            // Persist Suggestions
            if (aiResponse.getSuggestions() != null) {
                for (ParsedResumeAiResponse.ParsedSuggestion sug : aiResponse.getSuggestions()) {
                    ResumeSuggestion suggestion = new ResumeSuggestion(
                            resume, sug.getCategory(), sug.getPriority(), sug.getIssue(), sug.getRecommendation()
                    );
                    suggestionRepository.save(suggestion);
                }
            }

            // Persist Warnings
            if (aiResponse.getWarnings() != null) {
                for (String w : aiResponse.getWarnings()) {
                    ResumeWarning warning = new ResumeWarning(resume, w);
                    warningRepository.save(warning);
                }
            }

            Resume updatedResume = resumeRepository.save(resume);
            return mapToDetailDto(updatedResume);

        } catch (Exception ex) {
            resume.setParsingStatus("FAILED");
            resumeRepository.save(resume);

            ResumeWarning failWarning = new ResumeWarning(resume, "Analysis failed: " + ex.getMessage());
            warningRepository.save(failWarning);

            throw new RuntimeException("Resume analysis error: " + ex.getMessage(), ex);
        }
    }

    public Resume getResumeWithOwnership(UUID resumeId, String email) {
        User user = getUser(email);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to access this resume");
        }
        return resume;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private ResumeResponseDto mapToResponseDto(Resume r) {
        ResumeResponseDto dto = new ResumeResponseDto();
        dto.setId(r.getId());
        dto.setFileName(r.getFileName());
        dto.setFileType(r.getFileType());
        dto.setFileSize(r.getFileSize());
        dto.setIsActive(r.getIsActive());
        dto.setParsingStatus(r.getParsingStatus());
        dto.setCandidateName(r.getCandidateName());
        dto.setCandidateEmail(r.getCandidateEmail());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }

    private ResumeDetailDto mapToDetailDto(Resume r) {
        ResumeDetailDto dto = new ResumeDetailDto();
        dto.setId(r.getId());
        dto.setFileName(r.getFileName());
        dto.setFileType(r.getFileType());
        dto.setFileSize(r.getFileSize());
        dto.setIsActive(r.getIsActive());
        dto.setParsingStatus(r.getParsingStatus());
        dto.setParsedText(r.getParsedText());
        dto.setCandidateName(r.getCandidateName());
        dto.setCandidateEmail(r.getCandidateEmail());
        dto.setCandidatePhone(r.getCandidatePhone());
        dto.setSummary(r.getSummary());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        List<ResumeSkill> skills = skillRepository.findAllByResumeId(r.getId());
        dto.setSkills(skills.stream()
                .map(s -> new ResumeDetailDto.SkillDto(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList()));

        List<ResumeEducation> educations = educationRepository.findAllByResumeId(r.getId());
        dto.setEducation(educations.stream().map(e -> {
            ResumeDetailDto.EducationDto ed = new ResumeDetailDto.EducationDto();
            ed.setId(e.getId());
            ed.setInstitution(e.getInstitution());
            ed.setDegree(e.getDegree());
            ed.setFieldOfStudy(e.getFieldOfStudy());
            ed.setStartYear(e.getStartYear());
            ed.setEndYear(e.getEndYear());
            ed.setGrade(e.getGrade());
            return ed;
        }).collect(Collectors.toList()));

        List<ResumeExperience> experiences = experienceRepository.findAllByResumeId(r.getId());
        dto.setExperiences(experiences.stream().map(ex -> {
            ResumeDetailDto.ExperienceDto exp = new ResumeDetailDto.ExperienceDto();
            exp.setId(ex.getId());
            exp.setCompany(ex.getCompany());
            exp.setPosition(ex.getPosition());
            exp.setLocation(ex.getLocation());
            exp.setStartDate(ex.getStartDate());
            exp.setEndDate(ex.getEndDate());
            exp.setIsCurrent(ex.getIsCurrent());
            exp.setDescription(ex.getDescription());
            return exp;
        }).collect(Collectors.toList()));

        List<ResumeProject> projects = projectRepository.findAllByResumeId(r.getId());
        dto.setProjects(projects.stream().map(p -> {
            ResumeDetailDto.ProjectDto pr = new ResumeDetailDto.ProjectDto();
            pr.setId(p.getId());
            pr.setTitle(p.getTitle());
            pr.setDescription(p.getDescription());
            pr.setTechnologies(p.getTechnologies());
            pr.setLink(p.getLink());
            return pr;
        }).collect(Collectors.toList()));

        List<ResumeSuggestion> suggestions = suggestionRepository.findAllByResumeId(r.getId());
        dto.setSuggestions(suggestions.stream().map(s -> {
            ResumeDetailDto.SuggestionDto sug = new ResumeDetailDto.SuggestionDto();
            sug.setId(s.getId());
            sug.setCategory(s.getCategory());
            sug.setPriority(s.getPriority());
            sug.setIssue(s.getIssue());
            sug.setRecommendation(s.getRecommendation());
            return sug;
        }).collect(Collectors.toList()));

        List<ResumeWarning> warnings = warningRepository.findAllByResumeId(r.getId());
        dto.setWarnings(warnings.stream().map(ResumeWarning::getWarningMessage).collect(Collectors.toList()));

        return dto;
    }
}
