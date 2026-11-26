package com.resumematcher.analytics;

import com.resumematcher.analytics.dto.AnalyticsDashboardDto;
import com.resumematcher.analytics.service.AnalyticsService;
import com.resumematcher.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalyticsDashboardDto dashboard = analyticsService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
