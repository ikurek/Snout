package com.nightcode.snout.shared.domain.usecase.persons

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.PersonEventPage
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single
class GetPersonEvents(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: PersonRepository,
) {
    suspend operator fun invoke(uuid: String, before: Instant? = null): Either<DataError, PersonEventPage> =
        repository.getEvents(getSelectedProjectId(), uuid, before)
}
