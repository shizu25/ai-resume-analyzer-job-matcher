package com.resumematcher.matching;

import com.resumematcher.common.ResourceNotFoundException;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.repository.JobRepository;
import com.resumematcher.matching.dto.JobMatchResponseDto;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.matching.service.JobMatchingService;
import com.resumematcher.matching.service.SkillNormalizer;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.entity.ResumeSkill;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.resume.repository.ResumeSkillRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobMatchingServiceTest {

    @Mock private JobMatchRepository jobMatchRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeSkillRepository resumeSkillRepository;
    @Mock private UserRepository userRepository;

    private JobMatchingService jobMatchingService;

    @BeforeEach
    void setUp() {
        jobMatchingService = new JobMatchingService(
                jobMatchRepository,
                jobRepository,
                resumeRepository,
                resumeSkillRepository,
                userRepository,
                new SkillNormalizer()
        );
    }

    @Test
    void calculatesWeighted80_20ScoreWithAliasesAndExactMatching() {
        User user = createUser("alice@example.com");
        Resume resume = createResume(user, true, "COMPLETED");
        Job job = createJob(
                "Senior Java Backend Engineer",
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "REST API", "Microservices"),
                List.of("Kubernetes", "Kafka", "Redis", "AWS", "Flyway")
        );

        // Resume has 5/6 required (RESTful APIs -> REST API) and 2/5 preferred (Redis, AWS)
        // Expected: req=83.3, pref=40.0, overall=round1(0.8*83.3333 + 0.2*40.0)=74.7
        List<ResumeSkill> resumeSkills = List.of(
                new ResumeSkill(resume, "Java", "Programming"),
                new ResumeSkill(resume, "Spring Boot", "Backend"),
                new ResumeSkill(resume, "PostgreSQL", "Database"),
                new ResumeSkill(resume, "Docker", "Cloud & DevOps"),
                new ResumeSkill(resume, "RESTful APIs", "Backend"),
                new ResumeSkill(resume, "React", "Frontend"),
                new ResumeSkill(resume, "AWS", "Cloud & DevOps"),
                new ResumeSkill(resume, "Redis", "Database")
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(resumeSkillRepository.findAllByResumeId(resume.getId())).thenReturn(resumeSkills);
        when(jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())).thenReturn(Optional.empty());
        when(jobMatchRepository.save(any(JobMatch.class))).thenAnswer(inv -> {
            JobMatch m = inv.getArgument(0);
            if (m.getId() == null) m.setId(UUID.randomUUID());
            return m;
        });

        JobMatchResponseDto result = jobMatchingService.matchResumeToJob(job.getId(), null, "alice@example.com");

        assertEquals(74.7, result.getMatchScore());
        assertEquals(83.3, result.getRequiredSkillMatchPct());
        assertEquals(40.0, result.getPreferredSkillMatchPct());
        assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "REST API"), result.getMatchedRequiredSkills());
        assertEquals(List.of("Microservices"), result.getMissingRequiredSkills());
        assertEquals(List.of("Redis", "AWS"), result.getMatchedPreferredSkills());
        assertEquals(List.of("Kubernetes", "Kafka", "Flyway"), result.getMissingPreferredSkills());
        assertFalse(result.getRecommendations().isEmpty());
    }

    @Test
    void usesRequiredPercentageAsOverallScoreWhenPreferredSkillsEmpty() {
        User user = createUser("alice@example.com");
        Resume resume = createResume(user, true, "COMPLETED");
        Job job = createJob(
                "Backend Developer",
                List.of("Java", "JavaScript", "PostgreSQL"),
                List.of()
        );

        // Resume only has "Java" (must NOT match "JavaScript") -> 1/3 = 33.3%
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(resumeSkillRepository.findAllByResumeId(resume.getId()))
                .thenReturn(List.of(new ResumeSkill(resume, "Java", "Programming")));
        when(jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())).thenReturn(Optional.empty());
        when(jobMatchRepository.save(any(JobMatch.class))).thenAnswer(inv -> {
            JobMatch m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        JobMatchResponseDto result = jobMatchingService.matchResumeToJob(job.getId(), null, "alice@example.com");

        assertEquals(33.3, result.getRequiredSkillMatchPct());
        assertEquals(0.0, result.getPreferredSkillMatchPct());
        assertEquals(33.3, result.getMatchScore());
        assertEquals(List.of("Java"), result.getMatchedRequiredSkills());
        assertEquals(List.of("JavaScript", "PostgreSQL"), result.getMissingRequiredSkills());
        assertTrue(result.getMatchedPreferredSkills().isEmpty());
    }

    @Test
    void upsertsExistingMatchWithoutCreatingDuplicate() {
        User user = createUser("alice@example.com");
        Resume resume = createResume(user, true, "COMPLETED");
        Job job = createJob("Java Developer", List.of("Java", "Docker"), List.of());

        UUID existingMatchId = UUID.randomUUID();
        JobMatch existing = new JobMatch();
        existing.setId(existingMatchId);
        existing.setUser(user);
        existing.setResume(resume);
        existing.setJob(job);
        existing.setMatchScore(50.0);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(jobRepository.findByIdAndIsActiveTrue(job.getId())).thenReturn(Optional.of(job));
        when(resumeSkillRepository.findAllByResumeId(resume.getId()))
                .thenReturn(List.of(
                        new ResumeSkill(resume, "Java", "Programming"),
                        new ResumeSkill(resume, "Docker", "Cloud & DevOps")
                ));
        when(jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId()))
                .thenReturn(Optional.of(existing));
        when(jobMatchRepository.save(any(JobMatch.class))).thenAnswer(inv -> inv.getArgument(0));

        JobMatchResponseDto result = jobMatchingService.matchResumeToJob(job.getId(), null, "alice@example.com");

        ArgumentCaptor<JobMatch> captor = ArgumentCaptor.forClass(JobMatch.class);
        verify(jobMatchRepository, times(1)).save(captor.capture());
        assertEquals(existingMatchId, captor.getValue().getId());
        assertEquals(100.0, result.getMatchScore());
    }

    @Test
    void throwsMeaningfulErrorWhenNoActiveResumeExists() {
        User user = createUser("alice@example.com");
        UUID jobId = UUID.randomUUID();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jobMatchingService.matchResumeToJob(jobId, null, "alice@example.com"));
        assertTrue(ex.getMessage().contains("No active resume found"));
    }

    @Test
    void throwsMeaningfulErrorWhenResumeParsingNotCompleted() {
        User user = createUser("alice@example.com");
        Resume unparsed = createResume(user, true, "UPLOADED");
        UUID jobId = UUID.randomUUID();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(unparsed));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> jobMatchingService.matchResumeToJob(jobId, null, "alice@example.com"));
        assertTrue(ex.getMessage().contains("not completed"));
        assertTrue(ex.getMessage().contains("UPLOADED"));
    }

    @Test
    void throwsAccessDeniedWhenMatchingAnotherUsersResume() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");
        Resume bobsResume = createResume(bob, true, "COMPLETED");
        UUID jobId = UUID.randomUUID();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(resumeRepository.findById(bobsResume.getId())).thenReturn(Optional.of(bobsResume));

        assertThrows(AccessDeniedException.class,
                () -> jobMatchingService.matchResumeToJob(jobId, bobsResume.getId(), "alice@example.com"));
    }

    @Test
    void throwsAccessDeniedWhenAccessingAnotherUsersMatchById() {
        User alice = createUser("alice@example.com");
        User bob = createUser("bob@example.com");
        Resume bobsResume = createResume(bob, true, "COMPLETED");
        Job job = createJob("Engineer", List.of("Java"), List.of());

        JobMatch bobsMatch = new JobMatch();
        bobsMatch.setId(UUID.randomUUID());
        bobsMatch.setUser(bob);
        bobsMatch.setResume(bobsResume);
        bobsMatch.setJob(job);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobMatchRepository.findById(bobsMatch.getId())).thenReturn(Optional.of(bobsMatch));

        assertThrows(AccessDeniedException.class,
                () -> jobMatchingService.getMatchById(bobsMatch.getId(), "alice@example.com"));
    }

    @Test
    void throwsNotFoundForInactiveOrMissingJob() {
        User user = createUser("alice@example.com");
        Resume resume = createResume(user, true, "COMPLETED");
        UUID jobId = UUID.randomUUID();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(jobRepository.findByIdAndIsActiveTrue(jobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobMatchingService.matchResumeToJob(jobId, null, "alice@example.com"));
    }

    @Test
    void batchMatchingRunsAcrossActiveJobsAndSortsDescending() {
        User user = createUser("alice@example.com");
        Resume resume = createResume(user, true, "COMPLETED");
        Job lowMatchJob = createJob("Python ML", List.of("Python", "PyTorch"), List.of());
        Job highMatchJob = createJob("Java Backend", List.of("Java", "Spring Boot"), List.of());

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(Optional.of(resume));
        when(resumeSkillRepository.findAllByResumeId(resume.getId())).thenReturn(List.of(
                new ResumeSkill(resume, "Java", "Programming"),
                new ResumeSkill(resume, "Spring Boot", "Backend")
        ));
        when(jobRepository.findAllByIsActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(lowMatchJob, highMatchJob));
        when(jobMatchRepository.findAllByUserIdAndResumeIdOrderByMatchScoreDesc(user.getId(), resume.getId()))
                .thenReturn(List.of());
        when(jobMatchRepository.save(any(JobMatch.class))).thenAnswer(inv -> {
            JobMatch m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        List<JobMatchResponseDto> results = jobMatchingService.runMatchesForResume(null, "alice@example.com");

        assertEquals(2, results.size());
        assertEquals("Java Backend", results.get(0).getJobTitle());
        assertEquals(100.0, results.get(0).getMatchScore());
        assertEquals("Python ML", results.get(1).getJobTitle());
        assertEquals(0.0, results.get(1).getMatchScore());
    }

    private User createUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole("ROLE_USER");
        return user;
    }

    private Resume createResume(User user, boolean isActive, String status) {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setUser(user);
        resume.setFileName("resume.pdf");
        resume.setIsActive(isActive);
        resume.setParsingStatus(status);
        return resume;
    }

    private Job createJob(String title, List<String> required, List<String> preferred) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setTitle(title);
        job.setCompany("Nimbus Cloud");
        job.setLocation("Bengaluru");
        job.setRequiredSkills(required);
        job.setPreferredSkills(preferred);
        job.setIsActive(true);
        return job;
    }
}
