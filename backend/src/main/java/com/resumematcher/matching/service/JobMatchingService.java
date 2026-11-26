package com.resumematcher.matching.service;

import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.matching.dto.JobMatchResponseDto;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.entity.ResumeSkill;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.repository.ResumeSkillRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private static final double REQUIRED_WEIGHT = 0.80;
    private static final double PREFERRED_WEIGHT = 0.20;

    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final UserRepository userRepository;
    private final SkillNormalizer skillNormalizer;

    public JobMatchingService(JobMatchRepository jobMatchRepository,
                              JobRepository jobRepository,
                              ResumeRepository resumeRepository,
                              ResumeSkillRepository resumeSkillRepository,
                              UserRepository userRepository,
                              SkillNormalizer skillNormalizer) {
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.userRepository = userRepository;
        this.skillNormalizer = skillNormalizer;
    }

    @Transactional
    public JobMatchResponseDto matchResumeToJob(UUID jobId, UUID resumeId, String email) {
        User currentUser = getUser(email);
        Resume resume = resolveValidatedResume(resumeId, currentUser);
        Job job = getActiveJob(jobId);

        Set<String> normalizedResumeSkills = loadNormalizedResumeSkills(resume.getId());
        JobMatch existing = jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId()).orElse(null);

        JobMatch saved = computeAndPersistMatch(currentUser, resume, job, normalizedResumeSkills, existing);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public JobMatchResponseDto getMatchForJob(UUID jobId, UUID resumeId, String email) {
        User currentUser = getUser(email);
        Resume resume = resolveValidatedResume(resumeId, currentUser);
        Job job = getActiveJob(jobId);

        JobMatch match = jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No match result found for job " + jobId + " and resume " + resume.getId()
                ));

        if (!match.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this match result");
        }

        return mapToDto(match);
    }

    @Transactional
    public List<JobMatchResponseDto> runMatchesForResume(UUID resumeId, String email) {
        User currentUser = getUser(email);
        Resume resume = resolveValidatedResume(resumeId, currentUser);

        Set<String> normalizedResumeSkills = loadNormalizedResumeSkills(resume.getId());
        List<Job> activeJobs = jobRepository.findAllByIsActiveTrueOrderByCreatedAtDesc();

        Map<UUID, JobMatch> existingByJobId = jobMatchRepository
                .findAllByUserIdAndResumeIdOrderByMatchScoreDesc(currentUser.getId(), resume.getId())
                .stream()
                .collect(Collectors.toMap(m -> m.getJob().getId(), Function.identity(), (a, b) -> a));

        List<JobMatch> savedMatches = new ArrayList<>(activeJobs.size());
        for (Job job : activeJobs) {
            JobMatch saved = computeAndPersistMatch(
                    currentUser, resume, job, normalizedResumeSkills, existingByJobId.get(job.getId())
            );
            savedMatches.add(saved);
        }

        return savedMatches.stream()
                .map(this::mapToDto)
                .sorted(Comparator
                        .comparing(JobMatchResponseDto::getMatchScore, Comparator.reverseOrder())
                        .thenComparing(JobMatchResponseDto::getRequiredSkillMatchPct, Comparator.reverseOrder())
                        .thenComparing(JobMatchResponseDto::getJobTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponseDto> getMatchesForResume(UUID resumeId, String email) {
        User currentUser = getUser(email);
        Resume resume = resolveValidatedResume(resumeId, currentUser);

        return jobMatchRepository.findAllByUserIdAndResumeIdOrderByMatchScoreDesc(currentUser.getId(), resume.getId())
                .stream()
                .filter(match -> Boolean.TRUE.equals(match.getJob().getIsActive()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobMatchResponseDto getMatchById(UUID matchId, String email) {
        User currentUser = getUser(email);
        JobMatch match = jobMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result not found with ID: " + matchId));

        if (!match.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this match result");
        }

        return mapToDto(match);
    }

    private JobMatch computeAndPersistMatch(User user,
                                             Resume resume,
                                             Job job,
                                             Set<String> normalizedResumeSkills,
                                             JobMatch existing) {
        List<String> requiredSkills = deduplicateJobSkills(job.getRequiredSkills());
        List<String> preferredSkills = deduplicateJobSkills(job.getPreferredSkills());

        SkillPartition requiredPartition = partitionSkills(requiredSkills, normalizedResumeSkills);
        SkillPartition preferredPartition = partitionSkills(preferredSkills, normalizedResumeSkills);

        double rawRequiredPct = requiredSkills.isEmpty()
                ? 100.0
                : (requiredPartition.matched().size() * 100.0) / requiredSkills.size();
        double rawPreferredPct = preferredSkills.isEmpty()
                ? 0.0
                : (preferredPartition.matched().size() * 100.0) / preferredSkills.size();

        double rawOverallScore = preferredSkills.isEmpty()
                ? rawRequiredPct
                : (REQUIRED_WEIGHT * rawRequiredPct) + (PREFERRED_WEIGHT * rawPreferredPct);

        double requiredPct = roundToOneDecimal(rawRequiredPct);
        double preferredPct = roundToOneDecimal(rawPreferredPct);
        double overallScore = roundToOneDecimal(rawOverallScore);

        List<String> combinedMatched = mergeUniqueSkills(requiredPartition.matched(), preferredPartition.matched());
        List<String> combinedMissing = mergeUniqueSkills(requiredPartition.missing(), preferredPartition.missing());
        List<String> recommendations = generateRecommendations(
                normalizedResumeSkills.isEmpty(),
                requiredPartition.missing(),
                preferredPartition.missing(),
                overallScore,
                job.getTitle()
        );

        JobMatch match = existing != null ? existing : new JobMatch();
        match.setUser(user);
        match.setResume(resume);
        match.setJob(job);
        match.setMatchScore(overallScore);
        match.setRequiredSkillMatchPct(requiredPct);
        match.setPreferredSkillMatchPct(preferredPct);
        match.setMatchedSkills(combinedMatched);
        match.setMissingSkills(combinedMissing);
        match.setRecommendations(recommendations);
        match.setCalculatedAt(OffsetDateTime.now());

        return jobMatchRepository.save(match);
    }

    private Resume resolveValidatedResume(UUID resumeId, User currentUser) {
        Resume resume;
        if (resumeId != null) {
            resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));
            if (!resume.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to access this resume");
            }
        } else {
            resume = resumeRepository.findByUserIdAndIsActiveTrue(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No active resume found. Please upload a resume and set it as active before matching."
                    ));
        }

        if (!"COMPLETED".equalsIgnoreCase(resume.getParsingStatus())) {
            throw new IllegalArgumentException(
                    "Resume parsing is not completed (current status: " + resume.getParsingStatus()
                            + "). Please analyze your resume before matching."
            );
        }

        return resume;
    }

    private Job getActiveJob(UUID jobId) {
        return jobRepository.findByIdAndIsActiveTrue(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Active job not found with ID: " + jobId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private Set<String> loadNormalizedResumeSkills(UUID resumeId) {
        List<ResumeSkill> skills = resumeSkillRepository.findAllByResumeId(resumeId);
        List<String> names = skills.stream()
                .map(ResumeSkill::getName)
                .collect(Collectors.toList());
        return skillNormalizer.normalizeSet(names);
    }

    private List<String> deduplicateJobSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String skill : skills) {
            if (skill != null) {
                String trimmed = skill.trim();
                String normalized = skillNormalizer.normalize(trimmed);
                if (!normalized.isEmpty()) {
                    unique.putIfAbsent(normalized, trimmed);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private SkillPartition partitionSkills(List<String> jobSkills, Set<String> normalizedSkillSet) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String jobSkill : jobSkills) {
            if (skillNormalizer.containsSkill(normalizedSkillSet, jobSkill)) {
                matched.add(jobSkill);
            } else {
                missing.add(jobSkill);
            }
        }
        return new SkillPartition(matched, missing);
    }

    private List<String> mergeUniqueSkills(List<String> first, List<String> second) {
        Map<String, String> merged = new LinkedHashMap<>();
        for (String skill : first) {
            merged.putIfAbsent(skillNormalizer.normalize(skill), skill);
        }
        for (String skill : second) {
            merged.putIfAbsent(skillNormalizer.normalize(skill), skill);
        }
        return new ArrayList<>(merged.values());
    }

    private List<String> generateRecommendations(boolean noResumeSkills,
                                                 List<String> missingRequired,
                                                 List<String> missingPreferred,
                                                 double overallScore,
                                                 String jobTitle) {
        List<String> recommendations = new ArrayList<>();

        if (noResumeSkills) {
            recommendations.add("No extracted skills were found on your active resume. Update your skills section and re-analyze your resume.");
        }

        if (!missingRequired.isEmpty()) {
            recommendations.add("Core skill gap: Add or demonstrate " + formatSkillList(missingRequired)
                    + " in your resume experience or projects section.");
        }

        if (!missingPreferred.isEmpty()) {
            recommendations.add("Bonus skills: Highlight or build experience in " + formatSkillList(missingPreferred)
                    + " to strengthen your preferred-skill alignment.");
        }

        if (overallScore >= 80.0) {
            recommendations.add("Strong match (" + overallScore + "%): Tailor your professional summary toward '"
                    + jobTitle + "' and consider applying.");
        } else if (overallScore >= 50.0) {
            recommendations.add("Moderate-to-strong fit (" + overallScore + "%): Emphasize relevant projects and quantify impact for '"
                    + jobTitle + "'.");
        } else {
            recommendations.add("Developing fit (" + overallScore + "%): Focus on building the missing core required skills before applying to '"
                    + jobTitle + "'.");
        }

        return recommendations;
    }

    private String formatSkillList(List<String> skills) {
        return skills.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private JobMatchResponseDto mapToDto(JobMatch match) {
        Job job = match.getJob();
        Resume resume = match.getResume();

        Set<String> matchedSet = skillNormalizer.normalizeSet(match.getMatchedSkills());
        List<String> requiredSkills = deduplicateJobSkills(job.getRequiredSkills());
        List<String> preferredSkills = deduplicateJobSkills(job.getPreferredSkills());

        SkillPartition reqPartition = partitionSkills(requiredSkills, matchedSet);
        SkillPartition prefPartition = partitionSkills(preferredSkills, matchedSet);

        JobMatchResponseDto dto = new JobMatchResponseDto();
        dto.setId(match.getId());
        dto.setJobId(job.getId());
        dto.setResumeId(resume.getId());
        dto.setResumeFileName(resume.getFileName());
        dto.setJobTitle(job.getTitle());
        dto.setCompany(job.getCompany());
        dto.setLocation(job.getLocation());
        dto.setMatchScore(match.getMatchScore());
        dto.setRequiredSkillMatchPct(match.getRequiredSkillMatchPct());
        dto.setPreferredSkillMatchPct(match.getPreferredSkillMatchPct());
        dto.setMatchedRequiredSkills(reqPartition.matched());
        dto.setMissingRequiredSkills(reqPartition.missing());
        dto.setMatchedPreferredSkills(prefPartition.matched());
        dto.setMissingPreferredSkills(prefPartition.missing());
        dto.setMatchedSkills(match.getMatchedSkills() != null ? new ArrayList<>(match.getMatchedSkills()) : List.of());
        dto.setMissingSkills(match.getMissingSkills() != null ? new ArrayList<>(match.getMissingSkills()) : List.of());
        dto.setRecommendations(match.getRecommendations() != null ? new ArrayList<>(match.getRecommendations()) : List.of());
        dto.setCalculatedAt(match.getCalculatedAt());
        dto.setUpdatedAt(match.getUpdatedAt());
        return dto;
    }

    private record SkillPartition(List<String> matched, List<String> missing) {}
}
