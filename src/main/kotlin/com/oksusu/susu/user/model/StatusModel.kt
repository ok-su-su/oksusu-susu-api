package com.oksusu.susu.user.model

import com.oksusu.susu.user.domain.Status
import com.oksusu.susu.user.domain.vo.StatusType

/** 상태 정보 모델 */
class StatusModel(
    val id: Long = -1,
    /** 상태 정보 타입 */
    val statusType: StatusType,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    val isActive: Boolean,
) {
    companion object {
         fun from(status: Status): StatusModel {
             return StatusModel(
                 id = status.id,
                 statusType = status.statusType,
                 isActive = status.isActive,
             )
         }
    }
}
