package com.resumematcher.analytics.service;

import com.resumematcher.analytics.dto.AnalyticsDashboardDto;
import com.resumematcher.analytics.dto.TopMatchedRoleDto;
import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.repository.JobApplicationRepository;
import com.resumematcher.job.entity.Job;
import com.resumematcher.matching.entity.JobMatch;
import com.resumematcher.matching.repository.JobMatchRepository;
import com.resumematcher.resume.entity.Resume;
import com.resumematcher.resume.repository.ResumeRepository;
import com.resumematcher.user.User;
import com.resumematcher.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AnalyticsService {

    private static final int TOP_MATCHED_ROLES_LIMIT = 5;

    private final JobApplicationRepository jobApplicationRepository;
    private final JobMatchRepository jobMatchRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public AnalyticsService(JobApplicationRepository jobApplicationRepository,
                            JobMatchRepository jobMatchRepository,
                            ResumeRepository resumeRepository,
                            UserRepository userRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardDto getDashboard(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        UUID userId = currentUser.getId();

        long totalApplications = jobApplicationRepository.countByUserId(userId);
        long awaitingResponse = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED);
        long interviewingCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEWING);
        long offerCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFER);
        long rejectedCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED);

        double interviewRate = totalApplications == 0
                ? 0.0
                : roundToOneDecimal(((interviewingCount + offerCount) * 100.0) / totalApplications);
        double offerRate = totalApplications == 0
                ? 0.0
                : roundToOneDecimal((offerCount * 100.0) / totalApplications);

        List<TopMatchedRoleDto> topMatchedRoles = resolveTopMatchedRoles(userId);

        AnalyticsDashboardDto dto = new AnalyticsDashboardDto();
        dto.setTotalApplications(totalApplications);
        dto.setAwaitingResponse(awaitingResponse);
        dto.setInterviewingCount(interviewingCount);
        dto.setOfferCount(offerCount);
        dto.setRejectedCount(rejectedCount);
        dto.setInterviewRate(interviewRate);
        dto.setOfferRate(offerRate);
        dto.setTopMatchedRoles(topMatchedRoles);
        return dto;
    }

    private List<TopMatchedRoleDto> resolveTopMatchedRoles(UUID userId) {
        Pageable topFivePage = PageRequest.of(0, TOP_MATCHED_ROLES_LIMIT);
        Optional<Resume> activeResume = resumeRepository.findByUserIdAndIsActiveTrue(userId);

        List<JobMatch> candidates = List.of();
        if (activeResume.isPresent()) {
            candidates = jobMatchRepository.findTopActiveMatchesByUserAndResume(
                    userId, activeResume.get().getId(), topFivePage
            );
        }

        if (candidates == null || candidates.isEmpty()) {
            candidates = jobMatchRepository.findTopActiveMatchesByUser(userId, topFivePage);
        }

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Comparator<JobMatch> deterministicOrder = Comparator
                .comparing(JobMatch::getMatchScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(JobMatch::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(m -> m.getJob() != null ? m.getJob().getTitle() : null,
                        Comparator.nullsLast(String::compareToIgnoreCase));

        List<JobMatch> sortedActiveMatches = candidates.stream()
                .filter(m -> m.getJob() != null && Boolean.TRUE.equals(m.getJob().getIsActive()))
                .sorted(deterministicOrder)
                .toList();

        Map<UUID, TopMatchedRoleDto> deduplicatedByJob = new LinkedHashMap<>();
        for (JobMatch match : sortedActiveMatches) {
            Job job = match.getJob();
            deduplicatedByJob.putIfAbsent(job.getId(), new TopMatchedRoleDto(
                    job.getId(),
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation(),
                    match.getMatchScore()
            ));
            if (deduplicatedByJob.size() >= TOP_MATCHED_ROLES_LIMIT) {
                break;
            }
        }

        return new ArrayList<>(deduplicatedByJob.values());
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
