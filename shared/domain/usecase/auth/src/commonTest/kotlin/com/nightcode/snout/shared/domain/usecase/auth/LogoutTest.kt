package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class LogoutTest :
    ShouldSpec({
        val repository = mock<AuthRepository>(autoUnit)
        val subject = Logout(repository)

        should("delegate to repository clear") {
            subject()
            verifySuspend { repository.clear() }
        }
    })
