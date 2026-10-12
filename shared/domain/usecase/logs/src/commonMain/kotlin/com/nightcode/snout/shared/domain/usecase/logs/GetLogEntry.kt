package com.nightcode.snout.shared.domain.usecase.logs

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.logs.LogRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogEntry
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single
class GetLogEntry(private val getSelectedProjectId: GetSelectedProjectId, private val repository: LogRepository) {
    suspend operator fun invoke(service: String, id: String, timestamp: Instant): Either<DataError, LogEntry> =
        repository.getLog(getSelectedProjectId(), service, id, timestamp)
}
