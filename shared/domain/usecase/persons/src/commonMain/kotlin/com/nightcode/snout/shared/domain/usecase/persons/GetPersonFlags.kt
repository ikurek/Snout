package com.nightcode.snout.shared.domain.usecase.persons

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class GetPersonFlags(private val getSelectedProjectId: GetSelectedProjectId, private val repository: PersonRepository) {
    suspend operator fun invoke(distinctId: String): Either<DataError, List<PersonFlag>> =
        repository.getFlags(getSelectedProjectId(), distinctId)
}
