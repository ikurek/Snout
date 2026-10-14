package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Single

/**
 * When to load project data: for the selected project at once, again whenever another project is selected, and
 * again for the same project after each of [changes] (a repository's `changes`, none by default).
 */
@Single
class ObserveProjectReloads(private val repository: AppStateRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(changes: Flow<Unit> = emptyFlow()): Flow<ProjectReload> = repository.observeSelectedProject()
        .filterNotNull()
        .map { it.projectId }
        .distinctUntilChanged()
        .flatMapLatest {
            changes.map { ProjectReload(projectChanged = false) }.onStart { emit(ProjectReload(projectChanged = true)) }
        }
}
