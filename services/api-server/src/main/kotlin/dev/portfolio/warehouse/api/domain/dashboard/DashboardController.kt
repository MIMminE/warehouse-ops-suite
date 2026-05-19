package dev.portfolio.warehouse.api.domain.dashboard

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService,
) {
    @GetMapping
    fun getDashboard(): DashboardResponse =
        dashboardService.getDashboard()
}
