package com.oksusu.susu.api.fixture

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.setNotNullExp
import com.oksusu.susu.domain.post.domain.Board

class FixtureUtil {
    companion object {
        private val monkey: FixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

        private val boardBuilder = monkey.giveMeBuilder(Board::class.java)
            .setNotNullExp(Board::id)
            .setNotNullExp(Board::name)
            .setNotNullExp(Board::isActive)
            .setNotNullExp(Board::seq)

        fun getBoard(): Board = boardBuilder.sample()
        fun getBoards(size: Int): List<Board> = boardBuilder.sampleList(size)
        fun getBoardBuilder(): ArbitraryBuilder<Board> = boardBuilder
    }
}
