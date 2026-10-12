package com.nightcode.snout.shared.domain.usecase.auth

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.organization.OrganizationRepository
import com.nightcode.snout.shared.domain.data.user.UserRepository
import org.koin.core.annotation.Single

/** Removes everything stored for the signed-in account, including the account itself, except the credentials. */
@Single
class ClearLocalData(
    private val appStateRepository: AppStateRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke() {
        appStateRepository.clearSelectedProject()
        organizationRepository.clear()
        userRepository.clear()
    }
}
