package com.resumematcher.job;

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
import com.resumematcher.job.service.JobService;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, userRepository);
    }

    @Test
    void createJobSetsCreatorDefaultsAndDeduplicatesSkills() {
        User user = createUser("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(UUID.randomUUID());
            return j;
        });

        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("  Backend Engineer  ");
        req.setCompany("Acme Tech");
        req.setLocation("Bengaluru");
        req.setWorkMode(WorkMode.HYBRID);
        req.setJobType(JobType.FULL_TIME);
        req.setExperienceLevel(ExperienceLevel.MID);
        req.setSalaryMin(1200000);
        req.setSalaryMax(1800000);
        req.setDescription("Build scalable APIs.");
        req.setRequiredSkills(List.of("Java", "java", " Spring Boot "));
        req.setPreferredSkills(List.of("Docker", "docker"));

        JobResponseDto dto = jobService.createJob(req, "alice@example.com");

        assertNotNull(dto.getId());
        assertEquals("Backend Engineer", dto.getTitle());
        assertEquals("INR", dto.getSalaryCurrency());
        assertEquals(SalaryPeriod.ANNUAL, dto.getSalaryPeriod());
        assertEquals(List.of("Java", "Spring Boot"), dto.getRequiredSkills());
        assertEquals(List.of("Docker"), dto.getPreferredSkills());
        assertTrue(dto.isCanEdit());
        assertEquals(user.getId(), dto.getCreatedById());
    }

    @Test
    void createJobThrowsWhenSalaryMinExceedsSalaryMax() {
        User user = createUser("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        CreateJobRequest req = new CreateJobRequest();
        req.setTitle("Backend Engineer");
        req.setCompany("Acme Tech");
        req.setLocation("Remote");
        req.setWorkMode(WorkMode.REMOTE);
        req.setJobType(JobType.FULL_TIME);
        req.setExperienceLevel(ExperienceLevel.MID);
        req.setSalaryMin(2000000);
        req.setSalaryMax(1000000);
        req.setDescription("Description");
        req.setRequiredSkills(List.of("Java"));

        assertThrows(IllegalArgumentException.class, () -> jobService.createJob(req, "alice@example.com"));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void listJobsUsesSpecificationAndComputesCanEditFlag() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");

        Job aliceJob = createJobEntity(alice, "Java Developer");
        Job seededJob = createJobEntity(null, "Seeded Cloud Job");
        Job bobJob = createJobEntity(bob, "Frontend Developer");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findAll(ArgumentMatchers.<Specification<Job>>any(), any(Sort.class)))
                .thenReturn(List.of(aliceJob, seededJob, bobJob));

        List<JobResponseDto> result = jobService.listJobs(
                "Developer", "Java", "Bengaluru", WorkMode.HYBRID, JobType.FULL_TIME, ExperienceLevel.MID, "alice@example.com"
        );

        assertEquals(3, result.size());
        assertTrue(result.get(0).isCanEdit());
        assertFalse(result.get(1).isCanEdit());
        assertFalse(result.get(2).isCanEdit());
    }

    @Test
    void updateJobByCreatorSucceeds() {
        User alice = createUser("alice@example.com");
        Job job = createJobEntity(alice, "Old Title");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateJobRequest req = new UpdateJobRequest();
        req.setTitle("Senior Java Developer");
        req.setSalaryPeriod(SalaryPeriod.HOURLY);
        req.setSalaryMin(1200);
        req.setSalaryMax(1800);

        JobResponseDto updated = jobService.updateJob(job.getId(), req, "alice@example.com");

        assertEquals("Senior Java Developer", updated.getTitle());
        assertEquals(SalaryPeriod.HOURLY, updated.getSalaryPeriod());
        assertEquals(1200, updated.getSalaryMin());
        assertEquals(1800, updated.getSalaryMax());
    }

    @Test
    void updateJobByOtherUserThrowsAccessDenied() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");
        Job aliceJob = createJobEntity(alice, "Java Developer");

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(bob));
        when(jobRepository.findByIdAndIsActiveTrue(aliceJob.getId())).thenReturn(Optional.of(aliceJob));

        UpdateJobRequest req = new UpdateJobRequest();
        req.setTitle("Hijacked Title");

        assertThrows(AccessDeniedException.class,
                () -> jobService.updateJob(aliceJob.getId(), req, "bob@example.com"));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void updateOrDeleteSeededJobThrowsAccessDenied() {
        User alice = createUser("alice@example.com");
        Job seededJob = createJobEntity(null, "Seeded Job");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(seededJob.getId())).thenReturn(Optional.of(seededJob));

        UpdateJobRequest req = new UpdateJobRequest();
        req.setTitle("Modified Seed Job");

        assertThrows(AccessDeniedException.class,
                () -> jobService.updateJob(seededJob.getId(), req, "alice@example.com"));
        assertThrows(AccessDeniedException.class,
                () -> jobService.deactivateJob(seededJob.getId(), "alice@example.com"));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void deactivateJobSoftDeletesWithoutHardDelete() {
        User alice = createUser("alice@example.com");
        Job aliceJob = createJobEntity(alice, "Java Developer");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(aliceJob.getId())).thenReturn(Optional.of(aliceJob));

        jobService.deactivateJob(aliceJob.getId(), "alice@example.com");

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        verify(jobRepository, never()).delete(any(Job.class));
        verify(jobRepository, never()).deleteById(any());
        assertFalse(captor.getValue().getIsActive());
    }

    @Test
    void getMissingOrInactiveJobThrowsNotFound() {
        User alice = createUser("alice@example.com");
        UUID missingId = UUID.randomUUID();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobRepository.findByIdAndIsActiveTrue(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobService.getJobById(missingId, "alice@example.com"));
    }

    private User createUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole("ROLE_USER");
        return user;
    }

    private Job createJobEntity(User creator, String title) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setCreatedBy(creator);
        job.setTitle(title);
        job.setCompany("Nimbus Cloud");
        job.setLocation("Bengaluru");
        job.setWorkMode(WorkMode.HYBRID);
        job.setJobType(JobType.FULL_TIME);
        job.setExperienceLevel(ExperienceLevel.MID);
        job.setSalaryCurrency("INR");
        job.setSalaryPeriod(SalaryPeriod.ANNUAL);
        job.setDescription("Build APIs");
        job.setRequiredSkills(List.of("Java", "Spring Boot"));
        job.setIsActive(true);
        return job;
    }
}
