package com.nightcode.snout.shared.domain.usecase.annotations

import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** When to load annotations: at once, when another project is selected, and after each change made in the app. */
@Single
class ObserveAnnotationReloads(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    operator fun invoke(): Flow<ProjectReload> = observeSelectedProjectId.reloads(repository.changes)
}
