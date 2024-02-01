package com.oksusu.susu.user.model

import com.oksusu.susu.user.domain.Status
import com.oksusu.susu.user.domain.vo.PenaltyType
import com.oksusu.susu.user.domain.vo.StatusType
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

/** 상태 정보 모델 */
class StatusModel(
    val id: Long = -1,
    /** 상태 정보 설명 */
    val description: String,
    /** 상태 정보 타입 */
    val statusType: StatusType,
    /** 패널티 타입 */
    val penaltyType: PenaltyType?,
    /** 형량 */
    val degree: Long?,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    val isActive: Boolean,
) {
    companion object {
         fun from(status: Status): StatusModel {
             return StatusModel(
                 id = status.id,
                 description = status.description,
                 statusType = status.statusType,
                 penaltyType = status.penaltyType,
                 degree = status.degree,
                 isActive = status.isActive,
             )
         }
    }
}
