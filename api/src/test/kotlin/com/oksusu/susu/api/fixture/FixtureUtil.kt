package com.oksusu.susu.api.fixture

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.setNotNullExp
import com.navercorp.fixturemonkey.kotlin.setPostCondition
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.domain.post.domain.Board
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo

class FixtureUtil {
    companion object {
        private val monkey: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

        private val boardBuilder = monkey.giveMeBuilder(Board::class.java)
            .setNotNullExp(Board::id)
            .setPostCondition { 0 < it.id }
            .setNotNullExp(Board::name)
            .setNotNullExp(Board::isActive)
            .setNotNullExp(Board::seq)

        fun getBoard(): Board = boardBuilder.sample()
        fun getBoards(size: Int): List<Board> = boardBuilder.sampleList(size)
        fun getBoardBuilder(): ArbitraryBuilder<Board> = boardBuilder

        private val postBuilder = monkey.giveMeBuilder(Post::class.java)
            .setNotNullExp(Post::id)
            .setPostCondition { 0 < it.id }
            .setNotNullExp(Post::uid)
            .setPostCondition { 0 < it.uid }
            .setNotNullExp(Post::boardId)
            .setPostCondition { 0 < it.boardId }
            .setPostCondition { it.boardId <= 5 }
            .setNotNullExp(Post::content)
            .setPostCondition { it.content.length <= 500 }

        fun getPost(): Post = postBuilder.sample()
        fun getPosts(size: Int): List<Post> = postBuilder.sampleList(size)
        fun getPostBuilder(): ArbitraryBuilder<Post> = postBuilder

        private val userBuilder = monkey.giveMeBuilder(User::class.java)
            .setNotNullExp(User::id)
            .setPostCondition { 0 < it.id }
            .setNotNullExp(User::oauthInfo)
            .setNotNullExp(User::name)
            .setPostCondition { it.name.length <= 100 }
            .setNotNullExp(User::role)

        fun getUser(): User = userBuilder.sample()
        fun getUsers(size: Int): List<User> = userBuilder.sampleList(size)
        fun getUserBuilder(): ArbitraryBuilder<User> = userBuilder

        private val voteOptionBuilder = monkey.giveMeBuilder(VoteOption::class.java)
            .setNotNullExp(VoteOption::id)
            .setPostCondition { 0 < it.id }
            .setNotNullExp(VoteOption::postId)
            .setPostCondition { 0 < it.postId }
            .setNotNullExp(VoteOption::content)
            .setPostCondition { it.content.length <= 100 }
            .setNotNullExp(VoteOption::seq)
            .setPostCondition { 0 < it.seq }

        fun getVoteOption(): VoteOption = voteOptionBuilder.sample()
        fun getVoteOptions(size: Int): List<VoteOption> = voteOptionBuilder.sampleList(size)
        fun getVoteOptionBuilder(): ArbitraryBuilder<VoteOption> = voteOptionBuilder

        fun getAuthUser() = AuthUserImpl(
            uid = 1L,
            context = AuthContextImpl(
                name = "name",
                role = AccountRole.USER,
                profileImageUrl = null,
                userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
            )
        )
    }
}
