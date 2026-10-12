package com.nightcode.snout.shared.domain.usecase.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogPage
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetServiceLogs(private val getSelectedProjectId: GetSelectedProjectId, private val repository: LogRepository) {
    suspend operator fun invoke(
        service: String,
        range: LogsRange,
        severity: LogSeverityFilter,
        query: String,
        cursor: String? = null,
    ): Either<DataError, LogPage> = repository.getLogs(getSelectedProjectId(), service, range, severity, query, cursor)
}
