package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class ClearLocalDataTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)
        val userRepository = mock<UserRepository>(autoUnit)
        val cacheRepository = mock<CacheRepository>(autoUnit)
        val subject = ClearLocalData(appStateRepository, organizationRepository, userRepository, cacheRepository)

        should("clear the selected project, organizations, account and cache and nothing else") {
            subject()
            verifySuspend(exhaustiveOrder) {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                userRepository.clear()
                cacheRepository.clear()
            }
        }
    })
