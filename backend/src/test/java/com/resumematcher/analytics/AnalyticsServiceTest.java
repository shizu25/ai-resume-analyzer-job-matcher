package com.resumematcher.analytics;

import com.resumematcher.analytics.dto.AnalyticsDashboardDto;
import com.resumematcher.analytics.service.AnalyticsService;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.repository.JobApplicationRepository;
import com.resumematcher.job.entity.Job;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private JobMatchRepository jobMatchRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                jobApplicationRepository,
                jobMatchRepository,
                resumeRepository,
                userRepository
        );
    }

    @Test
    void zeroApplicationsAndZeroMatchesReturnsZeroRatesAndEmptyList() {
        User alice = createUser("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobApplicationRepository.countByUserId(alice.getId())).thenReturn(0L);
        when(jobApplicationRepository.countByUserIdAndStatus(eq(alice.getId()), any())).thenReturn(0L);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.empty());
        when(jobMatchRepository.findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class)))
                .thenReturn(List.of());

        AnalyticsDashboardDto dto = analyticsService.getDashboard("alice@example.com");

        assertEquals(0L, dto.getTotalApplications());
        assertEquals(0L, dto.getAwaitingResponse());
        assertEquals(0L, dto.getInterviewingCount());
        assertEquals(0L, dto.getOfferCount());
        assertEquals(0L, dto.getRejectedCount());
        assertEquals(0.0, dto.getInterviewRate());
        assertEquals(0.0, dto.getOfferRate());
        assertNotNull(dto.getTopMatchedRoles());
        assertTrue(dto.getTopMatchedRoles().isEmpty());
    }

    @Test
    void mixedStatusesCalculateRatesAndIsolateAuthenticatedUser() {
        User alice = createUser("alice@example.com");
        Resume activeResume = createResume(alice);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobApplicationRepository.countByUserId(alice.getId())).thenReturn(8L);
        when(jobApplicationRepository.countByUserIdAndStatus(alice.getId(), ApplicationStatus.APPLIED)).thenReturn(3L);
        when(jobApplicationRepository.countByUserIdAndStatus(alice.getId(), ApplicationStatus.INTERVIEWING)).thenReturn(2L);
        when(jobApplicationRepository.countByUserIdAndStatus(alice.getId(), ApplicationStatus.OFFER)).thenReturn(1L);
        when(jobApplicationRepository.countByUserIdAndStatus(alice.getId(), ApplicationStatus.REJECTED)).thenReturn(2L);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.of(activeResume));

        Job job = createJob("Senior Java Backend Engineer", true);
        JobMatch match = createMatch(alice, activeResume, job, 74.7, OffsetDateTime.now());
        when(jobMatchRepository.findTopActiveMatchesByUserAndResume(eq(alice.getId()), eq(activeResume.getId()), any(Pageable.class)))
                .thenReturn(List.of(match));

        AnalyticsDashboardDto dto = analyticsService.getDashboard("alice@example.com");

        assertEquals(8L, dto.getTotalApplications());
        assertEquals(3L, dto.getAwaitingResponse());
        assertEquals(2L, dto.getInterviewingCount());
        assertEquals(1L, dto.getOfferCount());
        assertEquals(2L, dto.getRejectedCount());
        assertEquals(37.5, dto.getInterviewRate());
        assertEquals(12.5, dto.getOfferRate());
        assertEquals(1, dto.getTopMatchedRoles().size());
        assertEquals(74.7, dto.getTopMatchedRoles().get(0).getMatchScore());

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobMatchRepository).findTopActiveMatchesByUserAndResume(eq(alice.getId()), eq(activeResume.getId()), pageCaptor.capture());
        assertEquals(0, pageCaptor.getValue().getPageNumber());
        assertEquals(5, pageCaptor.getValue().getPageSize());
        verify(jobMatchRepository, never()).findTopActiveMatchesByUser(any(), any());
    }

    @Test
    void noActiveResumeFallsBackToUserMatchesWithDatabaseLimit() {
        User alice = createUser("alice@example.com");
        Resume olderResume = createResume(alice);
        Job job = createJob("Python AI Engineer", true);
        JobMatch match = createMatch(alice, olderResume, job, 88.0, OffsetDateTime.now());

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobApplicationRepository.countByUserId(alice.getId())).thenReturn(0L);
        when(jobApplicationRepository.countByUserIdAndStatus(eq(alice.getId()), any())).thenReturn(0L);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.empty());
        when(jobMatchRepository.findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class)))
                .thenReturn(List.of(match));

        AnalyticsDashboardDto dto = analyticsService.getDashboard("alice@example.com");

        assertEquals(1, dto.getTopMatchedRoles().size());
        assertEquals("Python AI Engineer", dto.getTopMatchedRoles().get(0).getJobTitle());
        verify(jobMatchRepository, never()).findTopActiveMatchesByUserAndResume(any(), any(), any());
        verify(jobMatchRepository).findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class));
    }

    @Test
    void activeResumeWithNoMatchesFallsBackToOtherUserMatches() {
        User alice = createUser("alice@example.com");
        Resume activeResume = createResume(alice);
        Resume previousResume = createResume(alice);
        Job job = createJob("Cloud Platform Engineer", true);
        JobMatch fallbackMatch = createMatch(alice, previousResume, job, 81.5, OffsetDateTime.now());

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobApplicationRepository.countByUserId(alice.getId())).thenReturn(0L);
        when(jobApplicationRepository.countByUserIdAndStatus(eq(alice.getId()), any())).thenReturn(0L);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.of(activeResume));
        when(jobMatchRepository.findTopActiveMatchesByUserAndResume(eq(alice.getId()), eq(activeResume.getId()), any(Pageable.class)))
                .thenReturn(List.of());
        when(jobMatchRepository.findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class)))
                .thenReturn(List.of(fallbackMatch));

        AnalyticsDashboardDto dto = analyticsService.getDashboard("alice@example.com");

        assertEquals(1, dto.getTopMatchedRoles().size());
        assertEquals(81.5, dto.getTopMatchedRoles().get(0).getMatchScore());
        verify(jobMatchRepository).findTopActiveMatchesByUserAndResume(eq(alice.getId()), eq(activeResume.getId()), any(Pageable.class));
        verify(jobMatchRepository).findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class));
    }

    @Test
    void deduplicatesByJobKeepingHighestScoreExcludesInactiveAndCapsAtFiveWithTieBreaking() {
        User alice = createUser("alice@example.com");
        Resume resumeA = createResume(alice);
        Resume resumeB = createResume(alice);
        OffsetDateTime now = OffsetDateTime.now();

        Job job1 = createJob("Role A", true);
        Job job2 = createJob("Role B", true);
        Job job3 = createJob("Role C", true);
        Job job4 = createJob("Role D", true);
        Job job5 = createJob("Role E", true);
        Job job6 = createJob("Role F", true);
        Job inactiveJob = createJob("Closed Role", false);

        // Duplicate job1 (90.0 vs 65.0), deactivated job (99.0), tie on 80.0 (newer updatedAt vs older),
        // tie on 80.0 + same updatedAt (title alphabetical), plus 6th distinct role to verify max 5 cap.
        List<JobMatch> candidates = List.of(
                createMatch(alice, resumeA, inactiveJob, 99.0, now),
                createMatch(alice, resumeA, job1, 65.0, now.minusDays(1)),
                createMatch(alice, resumeB, job1, 90.0, now),
                createMatch(alice, resumeA, job2, 80.0, now.minusHours(2)),
                createMatch(alice, resumeA, job3, 80.0, now),
                createMatch(alice, resumeA, job4, 80.0, now),
                createMatch(alice, resumeA, job5, 70.0, now),
                createMatch(alice, resumeA, job6, 60.0, now)
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(jobApplicationRepository.countByUserId(alice.getId())).thenReturn(0L);
        when(jobApplicationRepository.countByUserIdAndStatus(eq(alice.getId()), any())).thenReturn(0L);
        when(resumeRepository.findByUserIdAndIsActiveTrue(alice.getId())).thenReturn(Optional.empty());
        when(jobMatchRepository.findTopActiveMatchesByUser(eq(alice.getId()), any(Pageable.class)))
                .thenReturn(candidates);

        AnalyticsDashboardDto dto = analyticsService.getDashboard("alice@example.com");

        assertEquals(5, dto.getTopMatchedRoles().size());
        assertEquals(job1.getId(), dto.getTopMatchedRoles().get(0).getJobId());
        assertEquals(90.0, dto.getTopMatchedRoles().get(0).getMatchScore());
        // Among 80.0 scores: job3 & job4 (now) precede job2 (now - 2h); Role C precedes Role D alphabetically
        assertEquals("Role C", dto.getTopMatchedRoles().get(1).getJobTitle());
        assertEquals("Role D", dto.getTopMatchedRoles().get(2).getJobTitle());
        assertEquals("Role B", dto.getTopMatchedRoles().get(3).getJobTitle());
        assertEquals("Role E", dto.getTopMatchedRoles().get(4).getJobTitle());
    }

    private User createUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole("ROLE_USER");
        return user;
    }

    private Resume createResume(User user) {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setUser(user);
        resume.setFileName("resume.pdf");
        resume.setIsActive(true);
        resume.setParsingStatus("COMPLETED");
        return resume;
    }

    private Job createJob(String title, boolean active) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setTitle(title);
        job.setCompany("Nimbus Cloud Systems");
        job.setLocation("Bengaluru, Karnataka");
        job.setIsActive(active);
        return job;
    }

    private JobMatch createMatch(User user, Resume resume, Job job, double score, OffsetDateTime updatedAt) {
        JobMatch match = new JobMatch();
        match.setId(UUID.randomUUID());
        match.setUser(user);
        match.setResume(resume);
        match.setJob(job);
        match.setMatchScore(score);
        match.setUpdatedAt(updatedAt);
        return match;
    }
}
