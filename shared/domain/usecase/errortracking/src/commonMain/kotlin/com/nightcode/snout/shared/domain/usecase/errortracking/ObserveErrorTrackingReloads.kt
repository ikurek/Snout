package com.nightcode.snout.shared.domain.usecase.errortracking

import com.nightcode.snout.shared.domain.data.errortracking.ErrorTrackingRepository
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** When to load issues: at once, when another project is selected, and after each change made in the app. */
@Single
class ObserveErrorTrackingReloads(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: ErrorTrackingRepository,
) {
    operator fun invoke(): Flow<ProjectReload> = observeSelectedProjectId.reloads(repository.changes)
}
