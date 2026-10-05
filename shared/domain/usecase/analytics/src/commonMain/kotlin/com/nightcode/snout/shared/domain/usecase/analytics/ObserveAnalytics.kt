package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ObserveAnalytics(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: AnalyticsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(range: AnalyticsRange): Flow<AnalyticsSnapshot?> =
        observeSelectedProjectId().flatMapLatest { repository.observeSnapshot(it, range) }
}
