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
import com.oksusu.susu.domain.user.domain.User

class DomainFixtureUtil {
    companion object {
        private val monkey: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

        private val boardBuilder = monkey.giveMeBuilder(Board::class.java)
            .set("id", -1)
            .setNotNullExp(Board::name)
            .setNotNullExp(Board::isActive)
            .setNotNullExp(Board::seq)

        fun getBoard(): Board = boardBuilder.sample()
        fun getBoards(size: Int): List<Board> = boardBuilder.sampleList(size)

        private val postBuilder = monkey.giveMeBuilder(Post::class.java)
            .set("id", -1)
            .setNotNullExp(Post::uid)
            .setPostCondition { 0 < it.uid }
            .setNotNullExp(Post::boardId)
            .setPostCondition { 0 < it.boardId }
            .setPostCondition { it.boardId <= 5 }
            .setNotNullExp(Post::content)
            .setPostCondition { it.content.length <= 500 }

        fun getPost(): Post = postBuilder.sample()
        fun getPosts(size: Int): List<Post> = postBuilder.sampleList(size)

        private val userBuilder = monkey.giveMeBuilder(User::class.java)
            .set("id", -1)
            .setNotNullExp(User::oauthInfo)
            .setNotNullExp(User::name)
            .setPostCondition { it.name.length <= 100 }
            .setNotNullExp(User::role)

        fun getUser(): User = userBuilder.sample()
        fun getUsers(size: Int): List<User> = userBuilder.sampleList(size)

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

        private val reportHistoryBuilder = monkey.giveMeBuilder(ReportHistory::class.java)
            .set("id", -1)
            .setNotNullExp(ReportHistory::targetType)
            .set("metadataId", 1L)

        fun getReportHistory(): ReportHistory = reportHistoryBuilder.sample()
        fun getReportHistorys(size: Int): List<ReportHistory> = reportHistoryBuilder
            .setNotNullExp(ReportHistory::targetId)
            .setPostCondition { 0 < it.targetId && it.targetId < size / 3 }
            .sampleList(size)


    }
}
