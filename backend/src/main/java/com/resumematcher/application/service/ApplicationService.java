package com.resumematcher.application.service;

import com.resumematcher.application.dto.ApplicationResponseDto;
import com.resumematcher.application.dto.CreateApplicationRequest;
import com.resumematcher.application.dto.UpdateApplicationRequest;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.entity.JobApplication;
import com.resumematcher.application.repository.JobApplicationRepository;
import com.resumematcher.common.ConflictException;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final String DUPLICATE_APPLICATION_CONSTRAINT = "uq_job_applications_user_job";

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final JobMatchRepository jobMatchRepository;
    private final UserRepository userRepository;

    public ApplicationService(JobApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              ResumeRepository resumeRepository,
                              JobMatchRepository jobMatchRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponseDto applyToJob(CreateApplicationRequest request, String email) {
        User currentUser = getUser(email);

        Job job = jobRepository.findByIdAndIsActiveTrue(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Active job not found with ID: " + request.getJobId()));

        if (applicationRepository.existsByUserIdAndJobId(currentUser.getId(), job.getId())) {
            throw new ConflictException("You have already applied to this job");
        }

        Resume resume = resolveOptionalResume(request.getResumeId(), currentUser);

        Double matchScoreSnapshot = null;
        if (resume != null) {
            matchScoreSnapshot = jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())
                    .map(JobMatch::getMatchScore)
                    .orElse(null);
        }

        JobApplication application = new JobApplication();
        application.setUser(currentUser);
        application.setJob(job);
        application.setResume(resume);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setMatchScoreSnapshot(matchScoreSnapshot);
        application.setNotes(trimToNull(request.getNotes()));

        try {
            JobApplication saved = applicationRepository.saveAndFlush(application);
            return mapToDto(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateApplicationViolation(ex)) {
                throw new ConflictException("You have already applied to this job", ex);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> listApplications(ApplicationStatus status, String email) {
        User currentUser = getUser(email);
        List<JobApplication> applications = status != null
                ? applicationRepository.findAllByUserIdAndStatusOrderByAppliedAtDesc(currentUser.getId(), status)
                : applicationRepository.findAllByUserIdOrderByAppliedAtDesc(currentUser.getId());

        return applications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponseDto getApplicationById(UUID applicationId, String email) {
        User currentUser = getUser(email);
        JobApplication application = getOwnedApplication(applicationId, currentUser);
        return mapToDto(application);
    }

    @Transactional
    public ApplicationResponseDto updateApplication(UUID applicationId, UpdateApplicationRequest request, String email) {
        if (request.getStatus() == null && request.getNotes() == null) {
            throw new IllegalArgumentException("At least one field (status or notes) must be provided");
        }

        User currentUser = getUser(email);
        JobApplication application = getOwnedApplication(applicationId, currentUser);

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            application.setNotes(trimToNull(request.getNotes()));
        }

        JobApplication updated = applicationRepository.save(application);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteApplication(UUID applicationId, String email) {
        User currentUser = getUser(email);
        JobApplication application = getOwnedApplication(applicationId, currentUser);
        applicationRepository.delete(application);
    }

    private Resume resolveOptionalResume(UUID resumeId, User currentUser) {
        if (resumeId != null) {
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));
            if (!resume.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to use this resume");
            }
            return resume;
        }
        return resumeRepository.findByUserIdAndIsActiveTrue(currentUser.getId()).orElse(null);
    }

    private JobApplication getOwnedApplication(UUID applicationId, User currentUser) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this application");
        }
        return application;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private boolean isDuplicateApplicationViolation(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConstraintViolationException cve) {
                String constraintName = cve.getConstraintName();
                if (constraintName != null
                        && constraintName.toLowerCase(Locale.ROOT).contains(DUPLICATE_APPLICATION_CONSTRAINT)) {
                    return true;
                }
            }
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains(DUPLICATE_APPLICATION_CONSTRAINT)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ApplicationResponseDto mapToDto(JobApplication application) {
        Job job = application.getJob();
        Resume resume = application.getResume();

        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(application.getId());
        dto.setJobId(job.getId());
        dto.setJobTitle(job.getTitle());
        dto.setCompany(job.getCompany());
        dto.setLocation(job.getLocation());
        dto.setWorkMode(job.getWorkMode());
        dto.setJobType(job.getJobType());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setSalaryCurrency(job.getSalaryCurrency());
        dto.setSalaryPeriod(job.getSalaryPeriod());
        dto.setJobActive(job.getIsActive());
        dto.setResumeId(resume != null ? resume.getId() : null);
        dto.setResumeFileName(resume != null ? resume.getFileName() : null);
        dto.setStatus(application.getStatus());
        dto.setMatchScoreSnapshot(application.getMatchScoreSnapshot());
        dto.setNotes(application.getNotes());
        dto.setAppliedAt(application.getAppliedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        return dto;
    }
}
