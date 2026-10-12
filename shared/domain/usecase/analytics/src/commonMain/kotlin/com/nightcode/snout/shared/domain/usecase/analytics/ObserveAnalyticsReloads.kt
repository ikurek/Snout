package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** When to load analytics: at once and when another project is selected. */
@Single
class ObserveAnalyticsReloads(private val observeSelectedProjectId: ObserveSelectedProjectId) {
    operator fun invoke(): Flow<ProjectReload> = observeSelectedProjectId.reloads()
}
