package dev.portfolio.warehouse.api.domain.printing

import com.fasterxml.jackson.databind.ObjectMapper
import dev.portfolio.warehouse.api.support.error.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
class PrintAgentClient(
    private val objectMapper: ObjectMapper,
    @Value("\${app.print.agent-url:http://localhost:4020}")
    private val printAgentUrl: String,
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build()

    fun enqueue(request: PrintAgentRequest): PrintAgentResponse {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$printAgentUrl/local/print-jobs"))
            .timeout(Duration.ofSeconds(5))
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
            .build()

        return try {
            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                throw BadRequestException("Print Agent가 출력 요청을 거부했습니다: ${response.statusCode()}")
            }
            objectMapper.readValue(response.body(), PrintAgentResponse::class.java)
        } catch (error: BadRequestException) {
            throw error
        } catch (error: Exception) {
            throw BadRequestException("Print Agent 전송에 실패했습니다: ${error.message}")
        }
    }
}

data class PrintAgentRequest(
    val jobNo: String,
    val documentType: String,
    val documentUrl: String,
    val printerName: String,
)

data class PrintAgentResponse(
    val jobNo: String,
    val documentType: String,
    val documentUrl: String,
    val printerName: String,
    val status: String,
    val failureReason: String?,
    val requestedAt: String,
)
