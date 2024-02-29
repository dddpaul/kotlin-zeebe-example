package com.github.dddpaul.zeebeexample.actuator

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class ApplicationStats {
    val created: AtomicLong = AtomicLong(0)
    val approved: AtomicLong = AtomicLong(0)
    val rejected: AtomicLong = AtomicLong(0)

    fun incrementCreated() {
        created.incrementAndGet()
    }

    fun incrementApproved() {
        approved.incrementAndGet()
    }

    fun incrementRejected() {
        rejected.incrementAndGet()
    }

    fun reset() {
        created.set(0)
        approved.set(0)
        rejected.set(0)
    }
}
