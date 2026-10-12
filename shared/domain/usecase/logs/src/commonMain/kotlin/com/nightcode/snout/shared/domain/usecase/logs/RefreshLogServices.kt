package com.nightcode.snout.shared.domain.usecase.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class RefreshLogServices(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: LogRepository,
) {
    suspend operator fun invoke(range: LogsRange): Either<DataError, Unit> =
        repository.refreshServices(getSelectedProjectId(), range)
}
