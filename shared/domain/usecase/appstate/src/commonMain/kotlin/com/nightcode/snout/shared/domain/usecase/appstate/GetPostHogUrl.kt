package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource

class GetPostHogUrl(private val authRepository: AuthRepository, private val appStateRepository: AppStateRepository) {

    suspend operator fun invoke(resource: PostHogResource): String? {
        val instanceUrl = authRepository.getInstanceUrl()
        val projectId = appStateRepository.getSelectedProject()?.projectId
        if (instanceUrl == null || projectId == null) return null
        return "$instanceUrl/project/$projectId/${resource.path()}"
    }
}
