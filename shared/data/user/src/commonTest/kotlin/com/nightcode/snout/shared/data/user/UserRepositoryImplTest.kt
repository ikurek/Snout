package com.nightcode.snout.shared.data.user

import com.nightcode.snout.shared.api.users.UsersApi
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.user.TestUser
import com.nightcode.snout.shared.storage.securestore.InMemorySecureStore
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first

private val TestUserJson = """
    {"date_joined":"2024-01-01T00:00:00Z","uuid":"00000000-0000-0000-0000-000000000002",
     "first_name":"John","last_name":"Doe","email":"john.doe@example.com","is_staff":false}
""".trimIndent()

@OptIn(TestData::class)
class UserRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val secureStore = InMemorySecureStore()

        fun repository() = UserRepositoryImpl(UsersApi(postHog.client, postHog.authDataStore), secureStore, TestJson)

        should("have no account before the first fetch") {
            repository().observeCurrentUser().first() shouldBe null
        }

        should("store the fetched account so a later launch shows it without fetching") {
            postHog.onRequest { respondJson(TestUserJson) }
            val repository = repository()

            repository.refreshCurrentUser().shouldBeRight()

            repository.observeCurrentUser().first() shouldBe TestUser
            secureStore.snapshot shouldContainKey CurrentUserKey
            repository().observeCurrentUser().first() shouldBe TestUser
            postHog.requests.size shouldBe 1
        }

        should("keep the stored account when a refresh fails") {
            postHog.onRequest { respondJson(TestUserJson) }
            val repository = repository()
            repository.refreshCurrentUser().shouldBeRight()

            postHog.onRequest { respondError(HttpStatusCode.ServiceUnavailable) }
            repository.refreshCurrentUser().shouldBeLeft() shouldBe DataError.UnexpectedError

            repository.observeCurrentUser().first() shouldBe TestUser
        }

        should("forget the account on clear") {
            postHog.onRequest { respondJson(TestUserJson) }
            val repository = repository()
            repository.refreshCurrentUser().shouldBeRight()

            repository.clear()

            repository.observeCurrentUser().first() shouldBe null
            secureStore.snapshot.shouldBeEmpty()
        }

        should("drop a stored record that no longer decodes") {
            secureStore.set(CurrentUserKey, "{not json")

            repository().observeCurrentUser().first() shouldBe null
            secureStore.snapshot.shouldBeEmpty()
        }
    })
