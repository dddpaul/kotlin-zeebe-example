package com.github.dddpaul.zeebeexample.workers

import com.fasterxml.jackson.databind.ObjectMapper
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.spring.client.annotation.JobWorker
import io.camunda.zeebe.spring.client.annotation.Variable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import com.github.dddpaul.zeebeexample.RiskError
import com.github.dddpaul.zeebeexample.RiskError.RISK_LEVEL_ERROR
import com.github.dddpaul.zeebeexample.RiskLevel
import com.github.dddpaul.zeebeexample.actuator.ApplicationStats
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
@ConditionalOnProperty(value = ["app.worker.enabled"], havingValue = "true")
class JobWorkers {
    private val client: HttpClient = HttpClient.newHttpClient()

    @Autowired
    private val stats: ApplicationStats? = null

    @JvmRecord
    internal data class LoopSettings(val retries: Int, val timeout: String)

    @JobWorker(type = "loop-settings")
    fun loopSettings(job: ActivatedJob): Map<String, Any> {
        var retries = 1
        var timeout = Duration.ofMinutes(20).toString()
        try {
            val request = HttpRequest.newBuilder()
                    .uri(URI("http://192.168.0.100:10000/params.json"))
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .GET()
                    .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val loopSettings = ObjectMapper().readValue(response.body(), LoopSettings::class.java)
            retries = loopSettings.retries
            timeout = loopSettings.timeout
        } catch (e: Exception) {
            log.warn("Application {} warning: {}", job.processInstanceKey, e.message)
        }
        log.info("Application {} loop is configured with retries = {} and timeout = {}", job.processInstanceKey, retries, timeout)
        return java.util.Map.of<String, Any>("retries", retries, "timeout", timeout)
    }

    @JobWorker(type = "risk-level")
    fun riskLevel(job: ActivatedJob, @Variable chance: Int): Map<String, Any> {
        try {
            if (chance >= RiskLevel.values().size) {
                throw RuntimeException("chance = %d is not acceptable".formatted(chance))
            }
            return java.util.Map.of("riskLevel", RiskLevel.values().get(chance).name.toLowerCase())
        } catch (e: Exception) {
            log.error("Application {} error: {}", job.processInstanceKey, e.message)
            throw RiskError.create(RISK_LEVEL_ERROR, e.message)
        }
    }

    @JobWorker(type = "approve-app")
    fun approve(job: ActivatedJob) {
        stats?.incrementApproved()
        log.info("Application {} approved", job.processInstanceKey)
    }

    @JobWorker(type = "reject-app")
    fun reject(job: ActivatedJob) {
        stats?.incrementRejected()
        log.info("Application {} rejected", job.processInstanceKey)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(JobWorkers::class.java)
    }
}
