package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.appstate.CacheRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository

/** Removes everything cached for the signed-in account except the credentials themselves. */
class ClearLocalData(
    private val appStateRepository: AppStateRepository,
    private val organizationRepository: OrganizationRepository,
    private val cacheRepository: CacheRepository,
) {
    suspend operator fun invoke() {
        appStateRepository.clearSelectedProject()
        organizationRepository.clear()
        cacheRepository.clear()
    }
}
