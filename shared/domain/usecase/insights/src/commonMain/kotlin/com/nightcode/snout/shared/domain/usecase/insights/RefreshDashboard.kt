package com.nightcode.snout.shared.domain.usecase.insights

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.insights.DashboardRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class RefreshDashboard(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(dashboardId: Long): Either<DataError, Unit> =
        repository.refreshDashboard(getSelectedProjectId(), dashboardId)
}
