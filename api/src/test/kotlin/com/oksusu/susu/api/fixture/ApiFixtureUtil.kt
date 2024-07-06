package fixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo

class ApiFixtureUtil {
    companion object {
        private val monkey: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

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
