package com.oksusu.susu.api.common.lock

private enum class LockType{
    VOTE,
    ;
}

class LockKey {
    companion object{
        fun getVoteKey(id: Long): String {
            return "${LockType.VOTE}_$id"
        }
    }
}
