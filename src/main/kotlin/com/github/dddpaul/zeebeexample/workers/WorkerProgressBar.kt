package com.github.dddpaul.zeebeexample.workers

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.MeterNotFoundException
import me.tongfei.progressbar.ProgressBarBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.worker.progress-bar.enabled"], havingValue = "true")
class WorkerProgressBar {
    @Autowired
    private val registry: MeterRegistry? = null

    @Throws(InterruptedException::class)
    fun start() {
        var i = 1
        while (true) {
            ProgressBarBuilder()
                    .setTaskName("Activated")
                    .showSpeed()
                    .setInitialMax(10000)
                    .build().use { activatedBar ->
                        ProgressBarBuilder()
                                .setTaskName("Completed")
                                .showSpeed()
                                .setInitialMax(10000)
                                .build().use { completedBar ->
                                    while (true) {
                                        try {
                                            val activatedCounter = registry!!["camunda.job.invocations"]
                                                    .tags("action", "activated")
                                                    .counter()
                                            val completedCounter = registry["camunda.job.invocations"]
                                                    .tags("action", "completed")
                                                    .counter()
                                            val a = Math.round(activatedCounter.count())
                                            val c = Math.round(completedCounter.count())
                                            if (a > i * activatedBar.max) {
                                                activatedBar.stepTo(activatedBar.max + a % activatedBar.max)
                                                completedBar.stepTo(activatedBar.max + c % completedBar.max)
                                                break
                                            }
                                            activatedBar.stepTo(a % activatedBar.max)
                                            completedBar.stepTo(c % completedBar.max)
                                            Thread.sleep(500)
                                        } catch (e: MeterNotFoundException) {
                                            Thread.sleep(500)
                                        }
                                    }
                                }
                    }
            i++
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WorkerProgressBar::class.java)
    }
}
