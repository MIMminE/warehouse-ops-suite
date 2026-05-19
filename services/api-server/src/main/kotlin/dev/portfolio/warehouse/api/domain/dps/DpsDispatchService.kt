package dev.portfolio.warehouse.api.domain.dps

import dev.portfolio.warehouse.api.domain.outbound.OutboundWaveRepository
import dev.portfolio.warehouse.api.domain.picking.PickingTaskRepository
import dev.portfolio.warehouse.api.support.error.BadRequestException
import dev.portfolio.warehouse.api.support.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class DpsDispatchService(
    private val outboundWaveRepository: OutboundWaveRepository,
    private val pickingTaskRepository: PickingTaskRepository,
    private val dpsAgentClient: DpsAgentClient,
) {
    @Transactional(readOnly = true)
    fun dispatchWave(waveId: Long): DpsDispatchResponse {
        val wave = outboundWaveRepository.findById(waveId)
            .orElseThrow { NotFoundException("출고 웨이브를 찾을 수 없습니다: $waveId") }
        val tasks = pickingTaskRepository.findByOutboundWaveIdOrderById(waveId)
            .filter { it.requestedQuantity > it.pickedQuantity }
        if (tasks.isEmpty()) {
            throw BadRequestException("DPS로 전송할 피킹 작업이 없습니다.")
        }

        val requestId = "dps-${UUID.randomUUID()}"
        val payload = DpsStartPickingBatchPayload(
            batchId = wave.waveNo,
            cells = tasks.map { task ->
                DpsPickingCellCommand(
                    cellCode = task.sourceLocation.code,
                    skuCode = task.sku.code,
                    quantity = task.requestedQuantity - task.pickedQuantity,
                )
            },
        )
        val agentResponse = dpsAgentClient.startPickingBatch(requestId, payload)
        if (agentResponse.type == "DPS_AGENT_ERROR") {
            throw BadRequestException("DPS Agent가 작업을 거부했습니다.")
        }

        return DpsDispatchResponse(
            waveId = requireNotNull(wave.id),
            waveNo = wave.waveNo,
            requestId = requestId,
            agentResponseType = agentResponse.type,
            cellCount = payload.cells.size,
            sentAt = Instant.now(),
        )
    }
}
