package com.github.dddpaul.zeebeexample.actuator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.stereotype.Component

@Component
@Endpoint(id = "stats")
class StatsEndpoint {
    @get:ReadOperation
    @Autowired
    val stats: ApplicationStats? = null

    @WriteOperation
    fun resetStats() {
        stats!!.reset()
    }
}
