package com.oksusu.susu.friend.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.friend.domain.Relationship
import com.oksusu.susu.friend.infrastructure.RelationshipRepository
import com.oksusu.susu.friend.model.RelationshipModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class RelationshipService(
    private val relationshipRepository: RelationshipRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var relationships: Map<Long, RelationshipModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-relationships.initial-delay:0}"
    )
    fun refreshRelationships() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh relationships" }

            relationships = runCatching {
                findAllByIsActive(true)
                    .map { relationship -> RelationshipModel.from(relationship) }
                    .associateBy { relationship -> relationship.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshRelationships", e)
            }.getOrDefault(relationships)

            logger.info { "finish refresh relationships" }
        }
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<Relationship> {
        return withContext(Dispatchers.IO) { relationshipRepository.findAllByIsActive(isActive) }
    }

    fun getRelationship(id: Long): RelationshipModel {
        return relationships[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_RELATIONSHIP_ERROR)
    }

    fun getAll(): List<RelationshipModel> {
        return relationships.values.toList()
    }
}
