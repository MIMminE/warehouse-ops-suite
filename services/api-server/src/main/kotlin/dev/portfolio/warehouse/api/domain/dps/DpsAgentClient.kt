package dev.portfolio.warehouse.api.domain.dps

import com.fasterxml.jackson.databind.ObjectMapper
import dev.portfolio.warehouse.api.support.error.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Component
class DpsAgentClient(
    private val objectMapper: ObjectMapper,
    @Value("\${app.dps.websocket-url:ws://localhost:4030/ws/dps}")
    private val websocketUrl: String,
) {
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    fun startPickingBatch(
        requestId: String,
        payload: DpsStartPickingBatchPayload,
    ): DpsAgentInboundEnvelope {
        val responseFuture = CompletableFuture<DpsAgentInboundEnvelope>()
        val listener = DpsResponseListener(objectMapper, requestId, responseFuture)
        val webSocket = httpClient
            .newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .buildAsync(URI.create(websocketUrl), listener)
            .getOrBadRequest("DPS Agent 연결에 실패했습니다.")

        val envelope = DpsAgentEnvelope(
            type = "PICKING_BATCH_STARTED",
            requestId = requestId,
            payload = payload,
        )

        return try {
            webSocket.sendText(objectMapper.writeValueAsString(envelope), true).join()
            responseFuture.get(5, TimeUnit.SECONDS)
        } catch (error: TimeoutException) {
            throw BadRequestException("DPS Agent 응답 시간이 초과되었습니다.")
        } catch (error: ExecutionException) {
            throw BadRequestException("DPS Agent 전송에 실패했습니다: ${error.cause?.message ?: error.message}")
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw BadRequestException("DPS Agent 전송이 중단되었습니다.")
        } finally {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "dispatch completed")
        }
    }
}

private fun <T> CompletableFuture<T>.getOrBadRequest(message: String): T =
    try {
        get(3, TimeUnit.SECONDS)
    } catch (error: TimeoutException) {
        throw BadRequestException("$message: 연결 시간이 초과되었습니다.")
    } catch (error: ExecutionException) {
        throw BadRequestException("$message: ${error.cause?.message ?: error.message}")
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        throw BadRequestException("$message: 연결이 중단되었습니다.")
    }

private class DpsResponseListener(
    private val objectMapper: ObjectMapper,
    private val requestId: String,
    private val responseFuture: CompletableFuture<DpsAgentInboundEnvelope>,
) : WebSocket.Listener {
    private val buffer = StringBuilder()

    override fun onOpen(webSocket: WebSocket) {
        webSocket.request(1)
    }

    override fun onText(
        webSocket: WebSocket,
        data: CharSequence,
        last: Boolean,
    ): CompletionStage<*> {
        buffer.append(data)
        if (last) {
            val rawMessage = buffer.toString()
            buffer.setLength(0)
            val envelope = objectMapper.readValue(rawMessage, DpsAgentInboundEnvelope::class.java)
            if (envelope.requestId == requestId || envelope.type == "DPS_AGENT_ERROR") {
                responseFuture.complete(envelope)
            }
        }
        webSocket.request(1)
        return CompletableFuture.completedFuture(null)
    }

    override fun onError(
        webSocket: WebSocket,
        error: Throwable,
    ) {
        responseFuture.completeExceptionally(
            BadRequestException("DPS Agent 연결에 실패했습니다: ${error.message}"),
        )
    }
}
