package com.github.dddpaul.zeebeexample

import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError
import java.util.Map

enum class RiskError(val code: String, val message: String) {
    RISK_LEVEL_ERROR(
            "RISK_LEVEL_ERROR",
            "Some error just happened: %s"
    );

    companion object {
        fun create(e: RiskError): ZeebeBpmnError {
            return ZeebeBpmnError(e.code, e.message)
        }

        fun create(e: RiskError, vararg args: Any?): ZeebeBpmnError {
            return ZeebeBpmnError(e.code, e.message.formatted(*args), Map.of())
        }
    }
}
