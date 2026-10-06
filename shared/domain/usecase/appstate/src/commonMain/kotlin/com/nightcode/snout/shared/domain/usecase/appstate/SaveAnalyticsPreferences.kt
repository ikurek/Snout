package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences

class SaveAnalyticsPreferences(private val repository: AppStateRepository) {
    suspend operator fun invoke(preferences: AnalyticsPreferences) {
        require(preferences.metrics.isNotEmpty()) { "At least one stat card must stay visible" }
        repository.saveAnalyticsPreferences(preferences)
    }
}
