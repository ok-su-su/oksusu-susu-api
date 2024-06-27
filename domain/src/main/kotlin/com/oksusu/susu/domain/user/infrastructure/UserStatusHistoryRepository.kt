package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.*
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>, UserStatusHistoryCustomRepository


interface UserStatusHistoryCustomRepository {
    @Transactional(readOnly = true)
    fun getUidByToStatusIdAfter(toStatusId: Long, targetDate: LocalDateTime): List<Long>

    @Transactional
    fun batchInsert(parameters: List<Map<String, Any>>)
}

class UserStatusHistoryCustomRepositoryImpl(
    @Qualifier("susuNamedParameterJdbcTemplate")
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : UserStatusHistoryCustomRepository, QuerydslRepositorySupport(UserStatusHistory::class.java) {

    companion object {
        private val INSERT_SQL = """
            insert into susu.user_status_history(uid, status_assignment_type, from_status_id, to_status_id, created_at, modified_at) 
            values (:uid, :status_assignment_type, :from_status_id, :to_status_id, :created_at, :modified_at) 
        """.trimIndent()
    }

    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qUserStatusHistory = QUserStatusHistory.userStatusHistory

    override fun getUidByToStatusIdAfter(toStatusId: Long, targetDate: LocalDateTime): List<Long> {
        return JPAQuery<QUserStatusHistory>(entityManager)
            .select(qUserStatusHistory.uid)
            .from(qUserStatusHistory)
            .where(
                qUserStatusHistory.toStatusId.eq(toStatusId),
                qUserStatusHistory.createdAt.after(targetDate)
            )
            .fetch()
    }

    override fun batchInsert(parameters: List<Map<String, Any>>) {
        val params = parameters.map { params -> MapSqlParameterSource().addValues(params) }
            .toTypedArray()

        jdbcTemplate.batchUpdate(INSERT_SQL, params)
    }
}
