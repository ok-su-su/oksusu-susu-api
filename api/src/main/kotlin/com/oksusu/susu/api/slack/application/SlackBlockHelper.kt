package com.oksusu.susu.api.slack.application

import com.oksusu.susu.api.slack.model.ErrorWebhookDataModel
import com.oksusu.susu.common.extension.remoteIp
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.composition.MarkdownTextObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class SlackBlockHelper {
    val logger = KotlinLogging.logger { }

    suspend fun getErrorBlocks(model: ErrorWebhookDataModel): List<LayoutBlock> {
        val request = model.request
        val e = model.exception

        val layoutBlock = mutableListOf<LayoutBlock>()

        val url = request.uri
        val method = request.method
        val body = DataBufferUtils.join(request.body)
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                bytes.decodeToString()
            }.awaitSingleOrNull()

        val errorMessage = e.toString()
        val errorStack = getErrorStack(e)
        val errorUserIP = request.remoteIp
        val errorRequestParam = getRequestParam(request)

        layoutBlock.add(
            Blocks.header { headerBlockBuilder: HeaderBlock.HeaderBlockBuilder ->
                headerBlockBuilder.text(
                    BlockCompositions.plainText("Error Detection")
                )
            }
        )
        layoutBlock.add(Blocks.divider())

        val errorUserIpMarkdown = MarkdownTextObject.builder()
            .text("* User IP :*\n$errorUserIP")
            .build()
        val methodMarkdown = MarkdownTextObject.builder()
            .text("* Request Addr :*\n$method : $url")
            .build()
        layoutBlock.add(
            Blocks.section { section ->
                section.fields(listOf(errorUserIpMarkdown, methodMarkdown))
            }
        )

        val requestParamMarkdown = MarkdownTextObject.builder()
            .text("* Requset Param :*\n$errorRequestParam")
            .build()
        val bodyMarkdown = MarkdownTextObject.builder().text("* Request Body :*\n$body").build()
        layoutBlock.add(
            Blocks.section { section ->
                section.fields(listOf(requestParamMarkdown, bodyMarkdown))
            }
        )

        layoutBlock.add(Blocks.divider())

        val errorNameMarkdown = MarkdownTextObject.builder().text("* Message :*\n$errorMessage").build()
        val errorStackMarkdown = MarkdownTextObject.builder().text("* Stack Trace :*\n$errorStack").build()
        layoutBlock.add(
            Blocks.section { section ->
                section.fields(listOf(errorNameMarkdown, errorStackMarkdown))
            }
        )

        return layoutBlock
    }

    private fun getErrorStack(e: Exception): String {
        val exceptionAsStrings = e.suppressedExceptions.flatMap { exception ->
            exception.stackTrace.map { stackTrace ->
                stackTrace.toString()
            }
        }.joinToString(" ")
        val cutLength = Math.min(exceptionAsStrings.length, 1000)
        return exceptionAsStrings.substring(0, cutLength)
    }

    private fun getRequestParam(request: ServerHttpRequest): String {
        return request.queryParams.map { param ->
            val value = if (param.value.size == 1) {
                param.value.firstOrNull()
            } else {
                param.value
            }
            "${param.key} : $value"
        }.joinToString("\n")
    }
}
