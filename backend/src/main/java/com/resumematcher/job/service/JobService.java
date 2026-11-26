package com.resumematcher.job.service;

import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.dto.CreateJobRequest;
import com.resumematcher.job.dto.JobResponseDto;
import com.resumematcher.job.dto.UpdateJobRequest;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.job.repository.JobSpecification;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> listJobs(String query,
                                         String skill,
                                         String location,
                                         WorkMode workMode,
                                         JobType jobType,
                                         ExperienceLevel experienceLevel,
                                         String email) {
        User currentUser = getUser(email);
        List<Job> jobs = jobRepository.findAll(
                JobSpecification.withFilters(query, skill, location, workMode, jobType, experienceLevel),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return jobs.stream()
                .map(job -> mapToDto(job, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobResponseDto getJobById(UUID jobId, String email) {
        User currentUser = getUser(email);
        Job job = getActiveJob(jobId);
        return mapToDto(job, currentUser);
    }

    @Transactional
    public JobResponseDto createJob(CreateJobRequest request, String email) {
        User currentUser = getUser(email);

        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());
        List<String> normalizedRequired = normalizeSkillList(request.getRequiredSkills());
        if (normalizedRequired.isEmpty()) {
            throw new IllegalArgumentException("At least one non-blank required skill must be provided");
        }
        List<String> normalizedPreferred = normalizeSkillList(request.getPreferredSkills());

        Job job = new Job();
        job.setCreatedBy(currentUser);
        job.setTitle(request.getTitle().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(request.getLocation().trim());
        job.setWorkMode(request.getWorkMode());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSalaryCurrency(
                request.getSalaryCurrency() != null && !request.getSalaryCurrency().isBlank()
                        ? request.getSalaryCurrency().trim().toUpperCase(Locale.ROOT)
                        : "INR"
        );
        job.setSalaryPeriod(request.getSalaryPeriod() != null ? request.getSalaryPeriod() : SalaryPeriod.ANNUAL);
        job.setDescription(request.getDescription().trim());
        job.setRequiredSkills(normalizedRequired);
        job.setPreferredSkills(normalizedPreferred);
        job.setApplicationUrl(trimToNull(request.getApplicationUrl()));
        job.setIsActive(true);

        Job saved = jobRepository.save(job);
        return mapToDto(saved, currentUser);
    }

    @Transactional
    public JobResponseDto updateJob(UUID jobId, UpdateJobRequest request, String email) {
        User currentUser = getUser(email);
        Job job = getActiveJob(jobId);
        assertOwnership(job, currentUser);

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new IllegalArgumentException("Job title cannot be blank");
            }
            job.setTitle(request.getTitle().trim());
        }
        if (request.getCompany() != null) {
            if (request.getCompany().isBlank()) {
                throw new IllegalArgumentException("Company name cannot be blank");
            }
            job.setCompany(request.getCompany().trim());
        }
        if (request.getLocation() != null) {
            if (request.getLocation().isBlank()) {
                throw new IllegalArgumentException("Location cannot be blank");
            }
            job.setLocation(request.getLocation().trim());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(request.getWorkMode());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }
        if (request.getExperienceLevel() != null) {
            job.setExperienceLevel(request.getExperienceLevel());
        }

        Integer nextMin = request.getSalaryMin() != null ? request.getSalaryMin() : job.getSalaryMin();
        Integer nextMax = request.getSalaryMax() != null ? request.getSalaryMax() : job.getSalaryMax();
        validateSalaryRange(nextMin, nextMax);
        job.setSalaryMin(nextMin);
        job.setSalaryMax(nextMax);

        if (request.getSalaryCurrency() != null && !request.getSalaryCurrency().isBlank()) {
            job.setSalaryCurrency(request.getSalaryCurrency().trim().toUpperCase(Locale.ROOT));
        }
        if (request.getSalaryPeriod() != null) {
            job.setSalaryPeriod(request.getSalaryPeriod());
        }
        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                throw new IllegalArgumentException("Job description cannot be blank");
            }
            job.setDescription(request.getDescription().trim());
        }
        if (request.getRequiredSkills() != null) {
            List<String> normalizedRequired = normalizeSkillList(request.getRequiredSkills());
            if (normalizedRequired.isEmpty()) {
                throw new IllegalArgumentException("At least one non-blank required skill must be provided");
            }
            job.setRequiredSkills(normalizedRequired);
        }
        if (request.getPreferredSkills() != null) {
            job.setPreferredSkills(normalizeSkillList(request.getPreferredSkills()));
        }
        if (request.getApplicationUrl() != null) {
            job.setApplicationUrl(trimToNull(request.getApplicationUrl()));
        }

        Job updated = jobRepository.save(job);
        return mapToDto(updated, currentUser);
    }

    @Transactional
    public void deactivateJob(UUID jobId, String email) {
        User currentUser = getUser(email);
        Job job = getActiveJob(jobId);
        assertOwnership(job, currentUser);

        job.setIsActive(false);
        jobRepository.save(job);
    }

    private Job getActiveJob(UUID jobId) {
        return jobRepository.findByIdAndIsActiveTrue(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Active job not found with ID: " + jobId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private void assertOwnership(Job job, User currentUser) {
        if (job.getCreatedBy() == null || !job.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify or delete this job posting");
        }
    }

    private void validateSalaryRange(Integer salaryMin, Integer salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary");
        }
    }

    private List<String> normalizeSkillList(List<String> skills) {
        if (skills == null) {
            return new ArrayList<>();
        }
        Map<String, String> deduplicated = new LinkedHashMap<>();
        for (String skill : skills) {
            if (skill != null) {
                String trimmed = skill.trim();
                if (!trimmed.isEmpty()) {
                    deduplicated.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
                }
            }
        }
        return new ArrayList<>(deduplicated.values());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private JobResponseDto mapToDto(Job job, User currentUser) {
        JobResponseDto dto = new JobResponseDto();
        dto.setId(job.getId());
        dto.setCreatedById(job.getCreatedBy() != null ? job.getCreatedBy().getId() : null);
        dto.setTitle(job.getTitle());
        dto.setCompany(job.getCompany());
        dto.setLocation(job.getLocation());
        dto.setWorkMode(job.getWorkMode());
        dto.setJobType(job.getJobType());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setSalaryCurrency(job.getSalaryCurrency());
        dto.setSalaryPeriod(job.getSalaryPeriod());
        dto.setDescription(job.getDescription());
        dto.setRequiredSkills(job.getRequiredSkills() != null ? new ArrayList<>(job.getRequiredSkills()) : List.of());
        dto.setPreferredSkills(job.getPreferredSkills() != null ? new ArrayList<>(job.getPreferredSkills()) : List.of());
        dto.setApplicationUrl(job.getApplicationUrl());
        dto.setIsActive(job.getIsActive());
        dto.setCanEdit(job.getCreatedBy() != null && job.getCreatedBy().getId().equals(currentUser.getId()));
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }
}
