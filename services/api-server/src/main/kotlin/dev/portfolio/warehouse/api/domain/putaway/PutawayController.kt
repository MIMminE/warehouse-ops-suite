package dev.portfolio.warehouse.api.domain.putaway

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/putaway-tasks")
class PutawayController(
    private val putawayService: PutawayService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreatePutawayTaskRequest,
    ): PutawayTaskResponse =
        putawayService.create(request)

    @PostMapping("/{taskId}/complete")
    fun complete(
        @PathVariable taskId: Long,
    ): PutawayTaskResponse =
        putawayService.complete(taskId)
}

data class CreatePutawayTaskRequest(
    @field:NotBlank
    val taskNo: String,
    @field:NotNull
    val receivingOrderLineId: Long,
    @field:NotNull
    val targetLocationId: Long,
    @field:Min(1)
    val putawayQuantity: Int,
    val assignedWorker: String?,
)

