package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class LogoutTest :
    ShouldSpec({
        val authRepository = mock<AuthRepository>(autoUnit)
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)
        val cacheRepository = mock<CacheRepository>(autoUnit)
        val subject = Logout(
            authRepository,
            ClearLocalData(appStateRepository, organizationRepository, cacheRepository),
        )

        should("clear local data and credentials") {
            subject()
            verifySuspend {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                cacheRepository.clear()
                authRepository.clear()
            }
        }
    })
