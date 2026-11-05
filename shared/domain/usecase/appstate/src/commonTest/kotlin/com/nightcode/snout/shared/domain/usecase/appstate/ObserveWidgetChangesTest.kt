package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.auth.PersonalApiKey
import com.nightcode.snout.shared.domain.model.auth.TestPersonalApiKey
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(TestData::class, ExperimentalCoroutinesApi::class)
class ObserveWidgetChangesTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>()
        val subject = ObserveWidgetChanges(authRepository)

        lateinit var key: MutableStateFlow<PersonalApiKey?>

        beforeEach {
            key = MutableStateFlow(TestPersonalApiKey)
            every { authRepository.observePersonalApiKey() } returns key
        }

        /** Runs [changes] while collecting, and returns how many times the subject emitted. */
        fun countChanges(changes: () -> Unit): Int {
            var count = 0
            runTest {
                val collector = launch { subject().collect { count++ } }
                runCurrent()
                changes()
                runCurrent()
                collector.cancel()
            }
            return count
        }

        should("not emit for the state the widgets already show") {
            countChanges {} shouldBe 0
        }

        should("emit when signing out") {
            countChanges { key.value = null } shouldBe 1
        }

        should("emit when signing in with another key, and not when the same key is saved again") {
            countChanges { key.value = TestPersonalApiKey.copy() } shouldBe 0
            countChanges { key.value = PersonalApiKey("phx_another") } shouldBe 1
        }
    })
