package com.resumematcher.analytics;

import com.resumematcher.analytics.dto.AnalyticsDashboardDto;
import com.resumematcher.analytics.dto.TopMatchedRoleDto;
import com.resumematcher.analytics.service.AnalyticsService;
import com.resumematcher.common.GlobalExceptionHandler;
import com.resumematcher.config.SecurityConfig;
import com.resumematcher.security.AuthEntryPointJwt;
import com.resumematcher.security.CustomUserDetailsService;
import com.resumematcher.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void dashboardWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void dashboardWithAuthReturns200AndAnalyticsPayload() throws Exception {
        UUID jobId = UUID.randomUUID();
        AnalyticsDashboardDto dto = new AnalyticsDashboardDto();
        dto.setTotalApplications(8L);
        dto.setAwaitingResponse(3L);
        dto.setInterviewingCount(2L);
        dto.setOfferCount(1L);
        dto.setRejectedCount(2L);
        dto.setInterviewRate(37.5);
        dto.setOfferRate(12.5);
        dto.setTopMatchedRoles(List.of(
                new TopMatchedRoleDto(jobId, "Senior Java Backend Engineer", "Nimbus Cloud Systems", "Bengaluru, Karnataka", 74.7)
        ));

        when(analyticsService.getDashboard("alice@example.com")).thenReturn(dto);

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalApplications").value(8))
                .andExpect(jsonPath("$.data.awaitingResponse").value(3))
                .andExpect(jsonPath("$.data.interviewingCount").value(2))
                .andExpect(jsonPath("$.data.offerCount").value(1))
                .andExpect(jsonPath("$.data.rejectedCount").value(2))
                .andExpect(jsonPath("$.data.interviewRate").value(37.5))
                .andExpect(jsonPath("$.data.offerRate").value(12.5))
                .andExpect(jsonPath("$.data.topMatchedRoles[0].jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.data.topMatchedRoles[0].jobTitle").value("Senior Java Backend Engineer"))
                .andExpect(jsonPath("$.data.topMatchedRoles[0].company").value("Nimbus Cloud Systems"))
                .andExpect(jsonPath("$.data.topMatchedRoles[0].matchScore").value(74.7));

        verify(analyticsService).getDashboard("alice@example.com");
    }
}
