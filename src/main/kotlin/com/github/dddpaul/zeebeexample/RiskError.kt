package com.github.dddpaul.zeebeexample

enum class RiskError(val code: String, val message: String) {
    RISK_LEVEL_ERROR(
            "RISK_LEVEL_ERROR",
            "Some error just happened: %s"
    );
}
