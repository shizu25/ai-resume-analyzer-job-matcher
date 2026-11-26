package com.resumematcher.application;

import com.resumematcher.application.dto.ApplicationResponseDto;
import com.resumematcher.application.dto.CreateApplicationRequest;
import com.resumematcher.application.dto.UpdateApplicationRequest;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.entity.JobApplication;
import com.resumematcher.application.repository.JobApplicationRepository;
import com.resumematcher.application.service.ApplicationService;
import com.resumematcher.common.ConflictException;
import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.SalaryPeriod;
import com.resumematcher.job.entity.WorkMode;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private JobMatchRepository jobMatchRepository;
    @Mock private UserRepository userRepository;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationRepository,
                jobRepository,
                resumeRepository,
                jobMatchRepository,
                userRepository
        );
    }

    @Test
    void applyWithActiveResumeAndSavedMatchCapturesSnapshot() {
        User user = createUser("alice@example.com");
        Job job = createJob("Senior Java Backend Engineer");
        Resume resume = createResume(user, "Jane_Resume.pdf");
        JobMatch match = new JobMatch();
        match.setMatchScore(74.7);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(user.getId(), job.getId())).thenReturn(false);
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())).thenReturn(Optional.of(match));
        when(applicationRepository.saveAndFlush(any(JobApplication.class))).thenAnswer(inv -> {
            JobApplication a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(job.getId());
        req.setNotes("  Submitted via career portal  ");

        ApplicationResponseDto dto = applicationService.applyToJob(req, "alice@example.com");

        assertNotNull(dto.getId());
        assertEquals(ApplicationStatus.APPLIED, dto.getStatus());
        assertEquals(resume.getId(), dto.getResumeId());
        assertEquals("Jane_Resume.pdf", dto.getResumeFileName());
        assertEquals(74.7, dto.getMatchScoreSnapshot());
        assertEquals("Submitted via career portal", dto.getNotes());
        assertEquals("Senior Java Backend Engineer", dto.getJobTitle());
        assertEquals("Nimbus Cloud Systems", dto.getCompany());
    }

    @Test
    void applyWithoutActiveResumeSucceedsWithNullResumeAndSnapshot() {
        User user = createUser("alice@example.com");
        Job job = createJob("Frontend React Engineer");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(user.getId(), job.getId())).thenReturn(false);
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.empty());
        when(applicationRepository.saveAndFlush(any(JobApplication.class))).thenAnswer(inv -> {
            JobApplication a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(job.getId());

        ApplicationResponseDto dto = applicationService.applyToJob(req, "alice@example.com");

        assertNull(dto.getResumeId());
        assertNull(dto.getResumeFileName());
        assertNull(dto.getMatchScoreSnapshot());
        assertEquals(ApplicationStatus.APPLIED, dto.getStatus());
        verify(jobMatchRepository, never()).findByResumeIdAndJobId(any(), any());
    }

    @Test
    void applyWithExplicitUnownedResumeThrowsAccessDenied() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");
        Job job = createJob("Backend Engineer");
        Resume bobsResume = createResume(bob, "Bob.pdf");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(alice.getId(), job.getId())).thenReturn(false);
        when(resumeRepository.findById(bobsResume.getId())).thenReturn(Optional.of(bobsResume));

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(job.getId());
        req.setResumeId(bobsResume.getId());

        assertThrows(AccessDeniedException.class,
                () -> applicationService.applyToJob(req, "alice@example.com"));
        verify(applicationRepository, never()).saveAndFlush(any());
    }

    @Test
    void applyToInactiveOrMissingJobThrowsNotFound() {
        User alice = createUser("alice@example.com");
        UUID jobId = UUID.randomUUID();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(jobId)).thenReturn(Optional.empty());

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(jobId);

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyToJob(req, "alice@example.com"));
    }

    @Test
    void duplicateApplicationThrowsConflict() {
        User alice = createUser("alice@example.com");
        Job job = createJob("Backend Engineer");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(alice.getId(), job.getId())).thenReturn(true);

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(job.getId());

        assertThrows(ConflictException.class,
                () -> applicationService.applyToJob(req, "alice@example.com"));
        verify(applicationRepository, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateConstraintConvertsToConflictWhileUnrelatedDataErrorPropagates() {
        User alice = createUser("alice@example.com");
        Job job = createJob("Backend Engineer");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserIdAndJobId(alice.getId(), job.getId())).thenReturn(false);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.empty());

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setJobId(job.getId());

        ConstraintViolationException duplicateCause = new ConstraintViolationException(
                "duplicate", new SQLException("duplicate key", "23505"), "uq_job_applications_user_job"
        );
        when(applicationRepository.saveAndFlush(any(JobApplication.class)))
                .thenThrow(new DataIntegrityViolationException("constraint", duplicateCause));

        assertThrows(ConflictException.class,
                () -> applicationService.applyToJob(req, "alice@example.com"));

        ConstraintViolationException unrelatedCause = new ConstraintViolationException(
                "not null", new SQLException("null value", "23502"), "chk_other_constraint"
        );
        when(applicationRepository.saveAndFlush(any(JobApplication.class)))
                .thenThrow(new DataIntegrityViolationException("unrelated", unrelatedCause));

        assertThrows(DataIntegrityViolationException.class,
                () -> applicationService.applyToJob(req, "alice@example.com"));
    }

    @Test
    void listApplicationsFiltersByAuthenticatedUserAndOptionalStatus() {
        User alice = createUser("alice@example.com");
        Job job = createJob("Backend Engineer");
        JobApplication app = new JobApplication();
        app.setId(UUID.randomUUID());
        app.setUser(alice);
        app.setJob(job);
        app.setStatus(ApplicationStatus.INTERVIEWING);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(applicationRepository.findAllByUserIdOrderByAppliedAtDesc(alice.getId())).thenReturn(List.of(app));
        when(applicationRepository.findAllByUserIdAndStatusOrderByAppliedAtDesc(alice.getId(), ApplicationStatus.INTERVIEWING))
                .thenReturn(List.of(app));

        assertEquals(1, applicationService.listApplications(null, "alice@example.com").size());
        assertEquals(1, applicationService.listApplications(ApplicationStatus.INTERVIEWING, "alice@example.com").size());
    }

    @Test
    void updateApplicationChangesStatusAndClearsEmptyNotes() {
        User alice = createUser("alice@example.com");
        Job job = createJob("Backend Engineer");
        JobApplication app = new JobApplication();
        app.setId(UUID.randomUUID());
        app.setUser(alice);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setNotes("Initial note");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateApplicationRequest req = new UpdateApplicationRequest();
        req.setStatus(ApplicationStatus.OFFER);
        req.setNotes("   ");

        ApplicationResponseDto updated = applicationService.updateApplication(app.getId(), req, "alice@example.com");

        assertEquals(ApplicationStatus.OFFER, updated.getStatus());
        assertNull(updated.getNotes());
    }

    @Test
    void updateWithBothStatusAndNotesNullThrowsIllegalArgument() {
        UpdateApplicationRequest emptyReq = new UpdateApplicationRequest();
        assertThrows(IllegalArgumentException.class,
                () -> applicationService.updateApplication(UUID.randomUUID(), emptyReq, "alice@example.com"));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    void ownershipChecksPreventOtherUserReadUpdateDelete() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");
        Job job = createJob("Backend Engineer");
        JobApplication bobsApp = new JobApplication();
        bobsApp.setId(UUID.randomUUID());
        bobsApp.setUser(bob);
        bobsApp.setJob(job);
        bobsApp.setStatus(ApplicationStatus.APPLIED);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(applicationRepository.findById(bobsApp.getId())).thenReturn(Optional.of(bobsApp));

        UpdateApplicationRequest req = new UpdateApplicationRequest();
        req.setStatus(ApplicationStatus.REJECTED);

        assertThrows(AccessDeniedException.class,
                () -> applicationService.getApplicationById(bobsApp.getId(), "alice@example.com"));
        assertThrows(AccessDeniedException.class,
                () -> applicationService.updateApplication(bobsApp.getId(), req, "alice@example.com"));
        assertThrows(AccessDeniedException.class,
                () -> applicationService.deleteApplication(bobsApp.getId(), "alice@example.com"));
        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void deleteApplicationRemovesOnlyApplicationRecord() {
        User alice = createUser("alice@example.com");
        Job job = createJob("Backend Engineer");
        JobApplication app = new JobApplication();
        app.setId(UUID.randomUUID());
        app.setUser(alice);
        app.setJob(job);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(applicationRepository.findById(app.getId())).thenReturn(Optional.of(app));

        applicationService.deleteApplication(app.getId(), "alice@example.com");

        verify(applicationRepository, times(1)).delete(app);
        verifyNoInteractions(jobMatchRepository);
        verify(jobRepository, never()).delete(any(Job.class));
        verify(resumeRepository, never()).delete(any());
    }

    private User createUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole("ROLE_USER");
        return user;
    }

    private Job createJob(String title) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setTitle(title);
        job.setCompany("Nimbus Cloud Systems");
        job.setLocation("Bengaluru, Karnataka");
        job.setWorkMode(WorkMode.HYBRID);
        job.setJobType(JobType.FULL_TIME);
        job.setExperienceLevel(ExperienceLevel.SENIOR);
        job.setSalaryMin(2200000);
        job.setSalaryMax(3200000);
        job.setSalaryCurrency("INR");
        job.setSalaryPeriod(SalaryPeriod.ANNUAL);
        job.setIsActive(true);
        return job;
    }

    private Resume createResume(User user, String fileName) {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setUser(user);
        resume.setFileName(fileName);
        resume.setIsActive(true);
        resume.setParsingStatus("COMPLETED");
        return resume;
    }
}
