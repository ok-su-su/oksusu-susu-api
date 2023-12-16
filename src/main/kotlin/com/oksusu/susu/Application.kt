package com.oksusu.susu

import com.oksusu.susu.extension.Zone
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.TimeZone

@EnableScheduling
@SpringBootApplication
class Application(
    private val buildProperties: BuildProperties,
    private val environment: Environment,
) : ApplicationListener<ApplicationReadyEvent> {
    private val logger = mu.KotlinLogging.logger { }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info(
            "{} applicationReady, profiles = {}",
            buildProperties.name,
            environment.activeProfiles.contentToString()
        )
    }
}

fun main(args: Array<String>) {
    /** Initialize jvm level configuration */
    init()
    runApplication<Application>(*args)
}

fun init() {
    /** Setting the Default TimeZone */
    TimeZone.setDefault(TimeZone.getTimeZone(Zone.KST))
}
