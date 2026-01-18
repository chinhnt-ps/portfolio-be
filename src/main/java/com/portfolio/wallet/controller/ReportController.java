package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.response.DashboardReportResponse;
import com.portfolio.wallet.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Report controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/reports/dashboard?period=month - Dashboard summary
 * - GET /api/v1/wallet/reports/monthly - Monthly report (future)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * Get dashboard report
     * 
     * @param period Period: "day", "week", "month", "year" (default: "month")
     * @param startDate Optional start date (ISO format: YYYY-MM-DDTHH:mm:ss)
     * @param endDate Optional end date (ISO format: YYYY-MM-DDTHH:mm:ss)
     */
    @GetMapping("/dashboard")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<DashboardReportResponse>> getDashboardReport(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        String userId = authentication.getName();
        DashboardReportResponse report = reportService.getDashboardReport(userId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Dashboard report retrieved successfully"));
    }
}
