package com.oksusu.susu.post.application

import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class VoteValidateService(
    private val postConfig: SusuConfig.PostConfig,
) {
    val logger = KotlinLogging.logger {  }

    fun validateCreateVoteRequest(request: CreateVoteRequest){
        val createPostForm = postConfig.createForm
        val createVoteForm = postConfig.createVoteForm
        val createVoteOptionForm = postConfig.createVoteOptionForm

        logger.info { request.content.length }

        if (request.content.length !in createPostForm.minContentLength .. createPostForm.maxContentLength){
            throw InvalidRequestException(ErrorCode.INVALID_POST_CONTENT_ERROR)
        }

        if (request.options.size < createVoteForm.minOptionCount){
            throw InvalidRequestException(ErrorCode.INVALID_VOTE_OPTION_ERROR)
        }

        request.options.forEach {option ->
            if (option.content.length !in createVoteOptionForm.minContentLength .. createVoteOptionForm.maxContentLength){
                throw InvalidRequestException(ErrorCode.INVALID_VOTE_OPTION_ERROR)
            }
        }

        request.options.map { option -> option.seq }.toSet().count { seq -> seq > 0 }.run {
            if (this != request.options.size) {
                throw InvalidRequestException(ErrorCode.INVALID_VOTE_OPTION_SEQUENCE)
            }
        }
    }

    fun validateUpdateVoteRequest(request: UpdateVoteRequest){
        val createPostForm = postConfig.createForm

        if (request.content.length !in createPostForm.minContentLength .. createPostForm.maxContentLength){
            throw InvalidRequestException(ErrorCode.INVALID_POST_CONTENT_ERROR)
        }
    }
}