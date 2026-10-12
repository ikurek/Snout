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

@Single
class ObserveSelectedProjectId(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<Long> = repository.observeSelectedProject()
        .filterNotNull()
        .map { it.projectId }
        .distinctUntilChanged()

    /** [observe]'s flow for the selected project, switched over whenever another project is selected. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> flatMapProject(observe: (projectId: Long) -> Flow<T>): Flow<T> = invoke().flatMapLatest(observe)

    /**
     * When to load project data: for the selected project at once, again whenever another project is selected, and
     * again for the same project after each of [changes] (a repository's `changes`).
     */
    fun reloads(changes: Flow<Unit> = emptyFlow()): Flow<ProjectReload> = flatMapProject { projectId ->
        changes.map { ProjectReload(projectId) }.onStart { emit(ProjectReload(projectId)) }
    }
}
