package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import org.koin.core.annotation.Single

@Single
class GetPostHogUrl(private val authRepository: AuthRepository, private val appStateRepository: AppStateRepository) {

    /** [resource] in [projectId] when given (a home-screen widget names its own), in the selected project otherwise. */
    suspend operator fun invoke(resource: PostHogResource, projectId: Long? = null): String? {
        val instanceUrl = authRepository.getInstanceUrl()
        val project = projectId ?: appStateRepository.getSelectedProject()?.projectId
        if (instanceUrl == null || project == null) return null
        return "$instanceUrl/project/$project/${resource.path()}"
    }
}
