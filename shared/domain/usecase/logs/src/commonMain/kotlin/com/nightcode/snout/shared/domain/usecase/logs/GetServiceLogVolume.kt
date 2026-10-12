package com.nightcode.snout.shared.domain.usecase.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogVolume
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetServiceLogVolume(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: LogRepository,
) {
    suspend operator fun invoke(
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
    ): Either<DataError, LogVolume> = repository.getVolume(getSelectedProjectId(), service, range, severity, query)
}
