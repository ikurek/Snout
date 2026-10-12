package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.ShouldSpec

class ClearLocalDataTest :
    ShouldSpec({
        val appStateRepository = mock<AppStateRepository>(autoUnit)
        val organizationRepository = mock<OrganizationRepository>(autoUnit)
        val cacheRepository = mock<CacheRepository>(autoUnit)
        val subject = ClearLocalData(appStateRepository, organizationRepository, cacheRepository)

        should("clear the selected project, organizations and cache and nothing else") {
            subject()
            verifySuspend(exhaustiveOrder) {
                appStateRepository.clearSelectedProject()
                organizationRepository.clear()
                cacheRepository.clear()
            }
        }
    })
