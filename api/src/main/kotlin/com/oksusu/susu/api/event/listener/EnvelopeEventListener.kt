package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.api.friend.application.FriendRelationshipService
import com.oksusu.susu.api.friend.application.FriendService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class EnvelopeEventListener(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val friendRelationshipService: FriendRelationshipService,
) {
    @TransactionalEventListener
    fun handel(event: DeleteEnvelopeEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            val count = envelopeService.countByUidAndFriendId(
                uid = event.uid,
                friendId = event.friendId
            )

            /** 조회되는 케이스가 없는 경우, 친구정보도 삭제 */
            if (count == 0L) {
                friendService.deleteSync(event.friendId)
                friendRelationshipService.deleteByFriendIdSync(event.friendId)
            }
        }
    }
}
