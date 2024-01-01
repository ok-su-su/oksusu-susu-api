package com.oksusu.susu.cache.helper

import com.oksusu.susu.common.consts.SUSU_CATEGORY_STATISTIC_KEY_PREFIX
import com.oksusu.susu.common.consts.SUSU_RELATIONSHIP_STATISTIC_KEY_PREFIX
import com.oksusu.susu.common.consts.SUSU_SPECIFIC_STATISTIC_KEY_PREFIX
import com.oksusu.susu.common.consts.USER_STATISTIC_KEY_PREFIX
import org.springframework.stereotype.Component

@Component
class CacheKeyGenerateHelper {
    suspend fun getUserStatisticKey(uid: Long): String {
        return "$USER_STATISTIC_KEY_PREFIX$uid"
    }

    suspend fun getSusuSpecificStatisticKey(age: Long, postCategoryId: Long, relationshipId: Long): String {
        return "$SUSU_SPECIFIC_STATISTIC_KEY_PREFIX$age:$postCategoryId:$relationshipId"
    }

    suspend fun getSusuSpecificStatisticKey(key: String): String {
        return "$SUSU_SPECIFIC_STATISTIC_KEY_PREFIX$key"
    }

    suspend fun getSusuCategoryStatisticKey(id: Long): String {
        return "$SUSU_CATEGORY_STATISTIC_KEY_PREFIX$id"
    }

    suspend fun getSusuRelationshipStatisticKey(id: Long): String {
        return "$SUSU_RELATIONSHIP_STATISTIC_KEY_PREFIX$id"
    }
}
