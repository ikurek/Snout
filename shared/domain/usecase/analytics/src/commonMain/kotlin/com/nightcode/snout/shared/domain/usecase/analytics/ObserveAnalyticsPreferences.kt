package com.nightcode.snout.shared.domain.usecase.analytics

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveAnalyticsPreferences(private val repository: AppStateRepository) {
    operator fun invoke(): Flow<AnalyticsPreferences> = repository.observeAnalyticsPreferences()
}
