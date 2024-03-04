package com.oksusu.susu.event.listener

import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.event.model.DeleteEnvelopeEvent
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.FriendService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EnvelopeEventListener(
    private val envelopeService: EnvelopeService,
    private val friendService: FriendService,
    private val friendRelationshipService: FriendRelationshipService,
) {
    @TransactionalEventListener
    fun handel(event: DeleteEnvelopeEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
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
