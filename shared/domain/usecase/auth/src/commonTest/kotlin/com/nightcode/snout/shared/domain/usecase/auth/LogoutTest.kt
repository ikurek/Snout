package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class LogoutTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>(autoUnit)
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)
        val userRepository = mock<UserRepository>(autoUnit)
        val cacheRepository = mock<CacheRepository>(autoUnit)
        val subject = Logout(
            authRepository,
            ClearLocalData(appStateRepository, organizationRepository, userRepository, cacheRepository),
        )

        should("clear local data and credentials") {
            subject()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                userRepository.clear()
                cacheRepository.clear()
                authRepository.clear()
            }
        }
    })
