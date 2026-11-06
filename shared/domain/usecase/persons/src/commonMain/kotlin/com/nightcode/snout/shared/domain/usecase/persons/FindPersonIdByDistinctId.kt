package com.nightcode.snout.shared.domain.usecase.persons

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

/** The uuid of the person PostHog's own links name by a distinct ID, so the app can open them. */
@Single
class FindPersonIdByDistinctId(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: PersonRepository,
) {
    suspend operator fun invoke(distinctId: String): Either<DataError, String> =
        repository.findPersonByDistinctId(getSelectedProjectId(), distinctId).map { it.uuid }
}
