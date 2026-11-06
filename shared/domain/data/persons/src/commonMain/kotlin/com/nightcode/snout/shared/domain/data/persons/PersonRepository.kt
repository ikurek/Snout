package com.nightcode.snout.shared.domain.data.persons

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.model.persons.PersonEventPage
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import kotlin.time.Instant

interface PersonRepository {
    suspend fun getPersons(projectId: Long, query: String): Either<DataError, List<Person>>

    suspend fun getPerson(projectId: Long, uuid: String): Either<DataError, Person>

    /** The person with exactly [distinctId]; [DataError.NotFound] when there is none. */
    suspend fun findPersonByDistinctId(projectId: Long, distinctId: String): Either<DataError, Person>

    suspend fun getEvents(projectId: Long, uuid: String, before: Instant?): Either<DataError, PersonEventPage>

    suspend fun getFlags(projectId: Long, distinctId: String): Either<DataError, List<PersonFlag>>

    suspend fun getExceptions(projectId: Long, uuid: String): Either<DataError, List<PersonException>>
}
