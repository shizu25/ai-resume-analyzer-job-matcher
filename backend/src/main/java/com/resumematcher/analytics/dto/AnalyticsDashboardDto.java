package com.resumematcher.analytics.dto;

import java.util.List;

public class AnalyticsDashboardDto {
    private long totalApplications;
    private long awaitingResponse;
    private long interviewingCount;
    private long offerCount;
    private long rejectedCount;
    private double interviewRate;
    private double offerRate;
    private List<TopMatchedRoleDto> topMatchedRoles;

    public AnalyticsDashboardDto() {}

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getAwaitingResponse() { return awaitingResponse; }
    public void setAwaitingResponse(long awaitingResponse) { this.awaitingResponse = awaitingResponse; }

    public long getInterviewingCount() { return interviewingCount; }
    public void setInterviewingCount(long interviewingCount) { this.interviewingCount = interviewingCount; }

    public long getOfferCount() { return offerCount; }
    public void setOfferCount(long offerCount) { this.offerCount = offerCount; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }

    public double getInterviewRate() { return interviewRate; }
    public void setInterviewRate(double interviewRate) { this.interviewRate = interviewRate; }

    public double getOfferRate() { return offerRate; }
    public void setOfferRate(double offerRate) { this.offerRate = offerRate; }

    public List<TopMatchedRoleDto> getTopMatchedRoles() { return topMatchedRoles; }
    public void setTopMatchedRoles(List<TopMatchedRoleDto> topMatchedRoles) { this.topMatchedRoles = topMatchedRoles; }
}
