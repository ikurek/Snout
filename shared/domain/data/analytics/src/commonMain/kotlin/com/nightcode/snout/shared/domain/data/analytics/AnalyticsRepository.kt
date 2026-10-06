package com.nightcode.snout.shared.domain.data.analytics

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeSnapshot(projectId: Long, range: AnalyticsRange): Flow<AnalyticsSnapshot?>

    /** Queries only what [preferences] shows, so disabled cards and tables cost no requests. */
    suspend fun refreshSnapshot(
        projectId: Long,
        range: AnalyticsRange,
        preferences: AnalyticsPreferences,
    ): Either<DataError, Unit>
}
