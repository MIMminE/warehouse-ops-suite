package dev.portfolio.warehouse.api.domain.picking

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/picking-tasks")
class PickingController(
    private val pickingService: PickingService,
) {
    @PostMapping("/{taskId}/confirm")
    fun confirm(
        @PathVariable taskId: Long,
        @Valid @RequestBody request: ConfirmPickingTaskRequest,
    ): PickingTaskResponse =
        pickingService.confirm(taskId, request.pickedQuantity)
}

data class ConfirmPickingTaskRequest(
    @field:Min(1)
    val pickedQuantity: Int,
)

