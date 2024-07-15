package com.oksusu.susu.client.common.coroutine

import com.oksusu.susu.client.slack.SlackClient
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.extension.LoggingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.errorStack
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.common.extension.isProd
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ErrorPublishingCoroutineExceptionHandler(
    private val slackClient: SlackClient,
    private val environment: Environment,
) {
    val handler = CoroutineExceptionHandler { _, throwable ->
        handle(throwable)
    }

    private val logger = KotlinLogging.logger { }

    private fun handle(exception: Throwable) {
        val errorMessage = exception.toString()
        val errorStack = exception.errorStack

        if (environment.isProd()) {
            CoroutineScope(Dispatchers.IO + LoggingCoroutineExceptionHandler).launch {
                slackClient.sendError(
                    SlackMessageModel(
                        """
                        * 스케줄러 에러 발생 ${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")}*
                        - Message : $errorMessage
                        - Stack Trace : $errorStack
                        """.trimIndent()
                    )
                )
            }
        }

        logger.error { errorStack }
    }
}
