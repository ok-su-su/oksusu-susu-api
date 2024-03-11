package com.oksusu.susu.api.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@JsonIgnoreProperties(value = ["createdAt, modifiedAt"], allowGetters = true)
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at", columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "Asia/Seoul")
    var createdAt: LocalDateTime = LocalDateTime.now()

    /** 수정일 */
    @LastModifiedDate
    @Column(name = "modified_at", columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "Asia/Seoul")
    var modifiedAt: LocalDateTime = LocalDateTime.now()
}
