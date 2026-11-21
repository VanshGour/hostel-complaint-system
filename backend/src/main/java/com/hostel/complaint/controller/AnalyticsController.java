package com.hostel.complaint.controller;

import com.hostel.complaint.dto.WeeklyAnalyticsResponse;
import com.hostel.complaint.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Operational metrics, MTTR calculations, and SLA statistics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/weekly-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get aggregated resolution metrics, MTTR, and SLA breach statistics")
    public ResponseEntity<WeeklyAnalyticsResponse> getWeeklySummary() {
        return ResponseEntity.ok(analyticsService.getWeeklySummary());
    }
}
