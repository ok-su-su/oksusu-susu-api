package com.oksusu.susu.api.bulk

import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.stereotype.Component
import org.testcontainers.shaded.org.apache.commons.io.FileUtils
import java.io.File
import java.time.LocalDateTime

@Component
class BulkService(
    private val txTemplates: TransactionTemplates,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    @Qualifier("susuNamedParameterJdbcTemplate")
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    companion object {
        private const val TEST_RESOURCES_PATH = "src/test/resources"
        private const val POST_SQL_PATH = "/scripts/bulk/post.txt"
        private const val VOTE_OPTION_SQL_PATH = "/scripts/bulk/voteOption.txt"
        private const val COUNT_SQL_PATH = "/scripts/bulk/count.txt"
        private const val CHUNK_SIZE = 100
    }

    val logger = KotlinLogging.logger { }

    suspend fun voteBulkInsert() {
        logger.info { "start vote bulk insert" }

        val users = withContext(Dispatchers.IO) {
            userRepository.findAll()
        }.sortedBy { user -> user.id }
        val firstUserId = users[0].id

        val boards = withContext(Dispatchers.IO) {
            boardRepository.findAllByIsActive(true)
        }.sortedBy { board -> board.id }
        val firstBoardId = boards[0].id

        val postDataSize = postBulkInsert(firstBoardId, firstUserId)

        val lastInsertedPostPk = getLastInsertedId()
        val firstInsertedPostPk = lastInsertedPostPk - postDataSize + 1

        val optionDataSize = voteOptionBulkInsert(firstInsertedPostPk)

        val lastInsertedVoteOptionPk = getLastInsertedId()
        val firstInsertedVoteOptionPk = lastInsertedVoteOptionPk - optionDataSize + 1

        countBulkInsert(firstInsertedPostPk, firstInsertedVoteOptionPk)

        logger.info { "finish vote bulk insert" }
    }

    private suspend fun getLastInsertedId(): Long {
        return withContext(Dispatchers.IO) {
            jdbcTemplate.queryForObject("SELECT last_insert_id()", MapSqlParameterSource(), Long::class.java)
        }!!
    }

    private suspend fun postBulkInsert(firstBoardId: Long, firstUserId: Long): Int {
        var dataSize = 0
        txTemplates.writer.coExecute(Dispatchers.IO) {
            val iterator = FileUtils.lineIterator(File(TEST_RESOURCES_PATH + POST_SQL_PATH), "UTF-8")

            try {
                while (true) {
                    if (!iterator.hasNext()) {
                        break
                    }

                    val lines = mutableListOf<String>()
                    for (i in 1..CHUNK_SIZE) {
                        if (iterator.hasNext()) {
                            lines.add(iterator.nextLine())
                            dataSize++
                        }
                    }

                    val sql = """
                        insert into susu.post(board_id, content, created_at, is_active, modified_at, title, type, uid) 
                        values (:board_id, :content, :created_at, :is_active, :modified_at, :title, :type, :uid) 
                    """.trimIndent()
                    jdbcTemplate.batchUpdate(sql, convertToPost(lines, firstBoardId, firstUserId))
                }
            } finally {
                iterator.close()
            }
        }
        return dataSize
    }

    private fun convertToPost(lines: List<String>, firstBoardId: Long, firstUserId: Long): Array<SqlParameterSource> {
        val maplist = lines.map { line ->
            val values = line.split(" ")
            listOf(
                "board_id" to values[0].toLong() + firstBoardId,
                "content" to values[1],
                "is_active" to values[2],
                "title" to values[3],
                "type" to values[4],
                "uid" to values[5].toLong() + firstUserId,
                "created_at" to LocalDateTime.now(),
                "modified_at" to LocalDateTime.now()
            ).toMap<String, Any>()
        }

        return maplist.map { params -> MapSqlParameterSource().addValues(params) }
            .toTypedArray()
    }

    private suspend fun voteOptionBulkInsert(firstPostId: Long): Int {
        var dataSize = 0
        txTemplates.writer.coExecute(Dispatchers.IO) {
            val iterator = FileUtils.lineIterator(File(TEST_RESOURCES_PATH + VOTE_OPTION_SQL_PATH), "UTF-8")

            try {
                while (true) {
                    if (!iterator.hasNext()) {
                        break
                    }

                    val lines = mutableListOf<String>()
                    for (i in 1..CHUNK_SIZE) {
                        if (iterator.hasNext()) {
                            lines.add(iterator.nextLine())
                            dataSize++
                        }
                    }

                    val sql = """
                        insert into susu.vote_option(content, created_at, modified_at, post_id, seq) 
                        values (:content, :created_at, :modified_at, :post_id, :seq) 
                    """.trimIndent()
                    jdbcTemplate.batchUpdate(sql, convertToVoteOption(lines, firstPostId))
                }
            } finally {
                iterator.close()
            }
        }
        return dataSize
    }

    private fun convertToVoteOption(lines: List<String>, firstPostId: Long): Array<SqlParameterSource> {
        val maplist = lines.map { line ->
            val values = line.split(" ")
            listOf(
                "content" to values[0],
                "post_id" to values[1].toLong() + firstPostId,
                "seq" to values[2],
                "created_at" to LocalDateTime.now(),
                "modified_at" to LocalDateTime.now()
            ).toMap<String, Any>()
        }

        return maplist.map { params -> MapSqlParameterSource().addValues(params) }
            .toTypedArray()
    }

    private suspend fun countBulkInsert(firstPostId: Long, firstVoteOptionId: Long): Int {
        var dataSize = 0
        txTemplates.writer.coExecute(Dispatchers.IO) {
            val iterator = FileUtils.lineIterator(File(TEST_RESOURCES_PATH + COUNT_SQL_PATH), "UTF-8")

            try {
                while (true) {
                    if (!iterator.hasNext()) {
                        break
                    }

                    val lines = mutableListOf<String>()
                    for (i in 1..CHUNK_SIZE) {
                        if (iterator.hasNext()) {
                            lines.add(iterator.nextLine())
                            dataSize++
                        }
                    }

                    val sql = """
                        insert into susu.count(count, count_type, created_at, modified_at, target_id, target_type)
                        values (:count, :count_type, :created_at, :modified_at, :target_id, :target_type)
                    """.trimIndent()
                    jdbcTemplate.batchUpdate(sql, convertToCount(lines, firstPostId, firstVoteOptionId))
                }
            } finally {
                iterator.close()
            }
        }
        return dataSize
    }

    private fun convertToCount(
        lines: List<String>,
        firstPostId: Long,
        firstVoteOptionId: Long,
    ): Array<SqlParameterSource> {
        val maplist = lines.map { line ->
            val values = line.split(" ")
            val baseId = if (values[3] == "0") {
                firstPostId
            } else {
                firstVoteOptionId
            }
            listOf(
                "count" to values[0],
                "count_type" to values[1],
                "target_id" to values[2].toLong() + baseId,
                "target_type" to values[3],
                "created_at" to LocalDateTime.now(),
                "modified_at" to LocalDateTime.now()
            ).toMap<String, Any>()
        }

        return maplist.map { params -> MapSqlParameterSource().addValues(params) }
            .toTypedArray()
    }
}
