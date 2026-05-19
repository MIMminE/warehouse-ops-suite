package dev.portfolio.warehouse.print

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.serialization.jackson.jackson
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

fun main() {
    val printQueue = PrintQueue()

    embeddedServer(Netty, port = 4020) {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
        routing {
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "ok",
                        "service" to "print-agent",
                        "queuedJobs" to printQueue.list().count { it.status == PrintJobStatus.QUEUED },
                    ),
                )
            }

            get("/local/printers") {
                call.respond(
                    listOf(
                        PrinterResponse("Zebra-ZD421-INV", "송장 프린터", "READY"),
                        PrinterResponse("HP-LaserJet-PICK", "피킹리스트 프린터", "READY"),
                        PrinterResponse("Mock-Failure-Printer", "실패 시뮬레이터", "OFFLINE"),
                    ),
                )
            }

            get("/local/print-jobs") {
                call.respond(printQueue.list())
            }

            get("/local/print-jobs/{jobNo}") {
                val jobNo = call.parameters["jobNo"].orEmpty()
                val job = printQueue.find(jobNo)
                if (job == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "print job not found"))
                } else {
                    call.respond(job)
                }
            }

            post("/local/print-jobs") {
                val request = call.receive<CreatePrintJobRequest>()
                val job = printQueue.enqueue(request)
                call.respond(HttpStatusCode.Accepted, job)
            }
        }
    }.start(wait = true)
}

private class PrintQueue {
    private val jobs = ConcurrentHashMap<String, PrintJobResponse>()

    fun enqueue(request: CreatePrintJobRequest): PrintJobResponse {
        val status = if (request.printerName.contains("Failure", ignoreCase = true)) {
            PrintJobStatus.FAILED
        } else {
            PrintJobStatus.QUEUED
        }
        val job = PrintJobResponse(
            jobNo = request.jobNo,
            documentType = request.documentType,
            documentUrl = request.documentUrl,
            printerName = request.printerName,
            status = status,
            failureReason = if (status == PrintJobStatus.FAILED) "프린터가 오프라인 상태입니다." else null,
            requestedAt = Instant.now(),
        )
        jobs[request.jobNo] = job
        return job
    }

    fun find(jobNo: String): PrintJobResponse? = jobs[jobNo]

    fun list(): List<PrintJobResponse> = jobs.values.sortedByDescending { it.requestedAt }
}

data class PrinterResponse(
    val name: String,
    val label: String,
    val status: String,
)

data class CreatePrintJobRequest(
    val jobNo: String,
    val documentType: String,
    val documentUrl: String,
    val printerName: String,
)

data class PrintJobResponse(
    val jobNo: String,
    val documentType: String,
    val documentUrl: String,
    val printerName: String,
    val status: PrintJobStatus,
    val failureReason: String?,
    val requestedAt: Instant,
)

enum class PrintJobStatus {
    QUEUED,
    FAILED,
}
