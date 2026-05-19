package dev.portfolio.warehouse.api.domain.dashboard

data class DashboardResponse(
    val metrics: DashboardMetricsResponse,
    val issueQueue: List<DashboardIssueResponse>,
    val clientSla: List<ClientSlaResponse>,
    val recentReceiving: List<DashboardListItemResponse>,
    val recentOutbound: List<DashboardListItemResponse>,
    val inventoryAlerts: List<DashboardListItemResponse>,
)

data class DashboardMetricsResponse(
    val receivingOrderCount: Int,
    val receivingRequestedQuantity: Int,
    val inventoryAvailableQuantity: Int,
    val inventoryAllocatedQuantity: Int,
    val inventoryHoldQuantity: Int,
    val outboundOrderCount: Int,
    val outboundNeedsAttentionCount: Int,
    val pickingTaskCount: Int,
    val pickingPickedQuantity: Int,
)

data class DashboardIssueResponse(
    val label: String,
    val count: Int,
    val description: String,
)

data class ClientSlaResponse(
    val clientCompanyName: String,
    val rate: Int,
)

data class DashboardListItemResponse(
    val label: String,
    val description: String,
    val status: String,
)
