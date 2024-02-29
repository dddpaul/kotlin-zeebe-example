package com.github.dddpaul.zeebeexample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import com.github.dddpaul.zeebeexample.workers.WorkerProgressBar

@SpringBootApplication
class ZeebeExampleApplication : ApplicationRunner {

    @Autowired(required = false)
    private val progressBar: WorkerProgressBar? = null

    @Value("\${app.worker.progress-bar.enabled:true}")
    private val workerProgressBarEnabled = false

    @Throws(InterruptedException::class)
    override fun run(args: ApplicationArguments) {
        if (workerProgressBarEnabled && progressBar != null) {
            progressBar.start()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ZeebeExampleApplication::class.java, *args)
        }
    }
}
