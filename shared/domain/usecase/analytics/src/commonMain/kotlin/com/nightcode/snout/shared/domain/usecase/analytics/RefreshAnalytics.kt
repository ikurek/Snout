package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId

class RefreshAnalytics(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnalyticsRepository,
) {
    suspend operator fun invoke(range: AnalyticsRange, preferences: AnalyticsPreferences): Either<DataError, Unit> =
        repository.refreshSnapshot(getSelectedProjectId(), range, preferences)
}
