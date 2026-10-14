package com.nightcode.snout.shared.domain.usecase.insights

import com.nightcode.snout.shared.domain.data.insights.InsightRepository
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** When to load insights: at once, when another project is selected, and after each change made in the app. */
@Single
class ObserveInsightReloads(
    private val observeProjectReloads: ObserveProjectReloads,
    private val repository: InsightRepository,
) {
    operator fun invoke(): Flow<ProjectReload> = observeProjectReloads(repository.changes)
}
