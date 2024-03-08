package com.github.dddpaul.zeebeexample.workers

import com.fasterxml.jackson.databind.ObjectMapper
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.spring.client.annotation.Variable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import com.github.dddpaul.zeebeexample.RiskError.RISK_LEVEL_ERROR
import com.github.dddpaul.zeebeexample.RiskLevel
import com.github.dddpaul.zeebeexample.actuator.ApplicationStats
import io.camunda.zeebe.client.api.worker.JobClient
import kotlinx.coroutines.future.await
import org.camunda.community.extension.coworker.spring.annotation.Coworker
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

    @Coworker(type = "loop-settings")
    suspend fun loopSettings(jobClient: JobClient, job: ActivatedJob) {
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
        jobClient.newCompleteCommand(job.key)
                .variables(mapOf("retries" to retries, "timeout" to timeout))
                .send()
                .await()
    }

    @Coworker(type = "risk-level")
    suspend fun riskLevel(jobClient: JobClient, job: ActivatedJob) {
        try {
            jobClient.newCompleteCommand(job.key)
                    .variables(mapOf("riskLevel" to RiskLevel.values().get(0).name.toLowerCase()))
                    .send()
                    .await()
        } catch (e: Exception) {
            log.error("Application {} error: {}", job.processInstanceKey, e.message)
            jobClient.newThrowErrorCommand(job.key)
                    .errorCode(RISK_LEVEL_ERROR.code)
                    .errorMessage(RISK_LEVEL_ERROR.message)
                    .send()
                    .await()
        }
    }

    @Coworker(type = "approve-app")
    suspend fun approve(jobClient: JobClient, job: ActivatedJob) {
        stats?.incrementApproved()
        log.info("Application {} approved", job.processInstanceKey)
        jobClient.newCompleteCommand(job.key)
                .send()
                .await()
    }

    @Coworker(type = "reject-app")
    suspend fun reject(jobClient: JobClient, job: ActivatedJob) {
        stats?.incrementRejected()
        log.info("Application {} rejected", job.processInstanceKey)
        jobClient.newCompleteCommand(job.key)
                .send()
                .await()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(JobWorkers::class.java)
    }
}
