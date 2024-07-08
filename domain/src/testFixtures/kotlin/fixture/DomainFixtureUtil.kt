package fixture

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.setNotNullExp
import com.oksusu.susu.domain.post.domain.Board
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.ReportResult
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.UserStatus
import com.oksusu.susu.domain.user.domain.UserStatusHistory

class DomainFixtureUtil {
    companion object {
        private val monkey: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

        /**
         * board
         */
        private val boardBuilder = monkey.giveMeBuilder(Board::class.java)
            .set("id", -1)
            .setNotNullExp(Board::name)
            .setNotNullExp(Board::isActive)
            .setNotNullExp(Board::seq)

        fun getBoard(): Board = boardBuilder.sample()
        fun getBoards(size: Int): List<Board> = boardBuilder.sampleList(size)

        /**
         * post
         */
        private val postBuilder = monkey.giveMeBuilder(Post::class.java)
            .set("id", -1)
            .setNotNullExp(Post::uid)
            .setNotNullExp(Post::boardId)
            .setNotNullExp(Post::content)
            .setNotNullExp(Post::type)
            .setPostCondition { 0 < it.uid }
            .setPostCondition { 0 < it.boardId }
            .setPostCondition { it.boardId <= 5 }
            .setPostCondition { it.content.length <= 500 }

        fun getPost(): Post = postBuilder.sample()
        fun getPosts(size: Int): List<Post> = postBuilder.sampleList(size)
        fun getPostBuilder(): ArbitraryBuilder<Post> = postBuilder

        /**
         * user
         */
        private val userBuilder = monkey.giveMeBuilder(User::class.java)
            .set("id", -1)
            .setNotNullExp(User::oauthInfo)
            .setNotNullExp(User::name)
            .setPostCondition { it.name.length <= 100 }
            .setNotNullExp(User::role)

        fun getUser(): User = userBuilder.sample()
        fun getUsers(size: Int): List<User> = userBuilder.sampleList(size)

        /**
         * vote option
         */
        private val voteOptionBuilder = monkey.giveMeBuilder(VoteOption::class.java)
            .set("id", -1)
            .setNotNullExp(VoteOption::postId)
            .setPostCondition { 0 < it.postId }
            .setNotNullExp(VoteOption::content)
            .setPostCondition { it.content.length <= 100 }
            .setNotNullExp(VoteOption::seq)
            .setPostCondition { 0 < it.seq }

        fun getVoteOption(): VoteOption = voteOptionBuilder.sample()
        fun getVoteOptions(size: Int): List<VoteOption> = voteOptionBuilder.sampleList(size)

        /**
         * report history
         */
        private val reportHistoryBuilder = monkey.giveMeBuilder(ReportHistory::class.java)
            .set("id", -1)
            .setNotNullExp(ReportHistory::uid)
            .setNotNullExp(ReportHistory::targetType)
            .setNotNullExp(ReportHistory::targetId)
            .set("metadataId", 1L)

        fun getReportHistory(): ReportHistory = reportHistoryBuilder.sample()
        fun getReportHistories(size: Int): List<ReportHistory> = reportHistoryBuilder
            .setPostCondition { 0 < it.targetId && it.targetId < size / 3 }
            .sampleList(size)

        fun getReportHistoryBuilder(): ArbitraryBuilder<ReportHistory> = reportHistoryBuilder

        /**
         * user status
         */
        private val userStatusBuilder = monkey.giveMeBuilder(UserStatus::class.java)
            .set("id", -1)
            .setNotNullExp(UserStatus::uid)
            .setNotNullExp(UserStatus::accountStatusId)
            .setNotNullExp(UserStatus::communityStatusId)
            .setPostCondition("accountStatusId", Long::class.java) { accountStatusId -> accountStatusId in 1..4 }
            .setPostCondition("communityStatusId", Long::class.java) { communityStatusId -> communityStatusId in 1..4 }

        fun getUserStatus(): UserStatus = userStatusBuilder.sample()
        fun getUserStatuses(size: Int): List<UserStatus> = userStatusBuilder
            .setPostCondition { 0 < it.uid && it.uid < size / 3 }
            .sampleList(size)

        fun getUserStatusBuilder(): ArbitraryBuilder<UserStatus> = userStatusBuilder

        /**
         * user status history
         */
        private val userStatusHistoryBuilder = monkey.giveMeBuilder(UserStatusHistory::class.java)
            .set("id", -1)
            .setNotNullExp(UserStatusHistory::uid)
            .setNotNullExp(UserStatusHistory::statusAssignmentType)
            .setNotNullExp(UserStatusHistory::fromStatusId)
            .setNotNullExp(UserStatusHistory::toStatusId)
            .setNotNullExp(UserStatusHistory::isForced)
            .setPostCondition("fromStatusId", Long::class.java) { fromStatusId -> fromStatusId in 1..4 }
            .setPostCondition("toStatusId", Long::class.java) { toStatusId -> toStatusId in 1..4 }

        fun getUserStatusHistory(): UserStatusHistory = userStatusHistoryBuilder.sample()
        fun getUserStatusHistories(size: Int): List<UserStatusHistory> = userStatusHistoryBuilder
            .setPostCondition { 0 < it.uid && it.uid < size / 3 }
            .sampleList(size)
        fun getUserStatusHistoryBuilder(): ArbitraryBuilder<UserStatusHistory> = userStatusHistoryBuilder

        /**
         * repost result
         */
        private val reportResultBuilder = monkey.giveMeBuilder(ReportResult::class.java)
            .set("id", -1)
            .setNotNullExp(ReportResult::targetId)
            .setNotNullExp(ReportResult::targetType)
            .setNotNullExp(ReportResult::status)

        fun getReportResult(): ReportResult = reportResultBuilder.sample()
        fun getReportResults(size: Int, minTargetId: Long, maxTargetId: Long): List<ReportResult> = reportResultBuilder
            .setPostCondition("targetId", Long::class.java) { targetId -> targetId in minTargetId..maxTargetId }
            .sampleList(size)
    }
}
