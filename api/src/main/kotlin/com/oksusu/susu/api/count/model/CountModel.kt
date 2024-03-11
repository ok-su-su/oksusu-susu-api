package com.oksusu.susu.api.count.model

import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.count.domain.vo.CountType

/** 카운트 모델 */
class CountModel(
    /** 카운트 id */
    val id: Long,
    /** 카운트 타켓 id */
    val targetId: Long,
    /** 카운트 타겟 타입 */
    val targetType: CountTargetType,
    /** 카운트 타입 */
    val countType: CountType,
    /** 카운트 */
    var count: Long = 0,
)
