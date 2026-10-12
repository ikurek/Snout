package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveAnalytics(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(range: AnalyticsRange): Flow<AnalyticsSnapshot?> =
        observeSelectedProjectId.flatMapProject { repository.observeSnapshot(it, range) }
}
