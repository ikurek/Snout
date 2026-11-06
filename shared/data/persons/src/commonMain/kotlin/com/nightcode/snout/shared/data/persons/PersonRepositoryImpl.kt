package com.nightcode.snout.shared.data.persons

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.nightcode.snout.shared.api.persons.PersonsApi
import com.nightcode.snout.shared.data.persons.mapper.toEventPage
import com.nightcode.snout.shared.data.persons.mapper.toExceptions
import com.nightcode.snout.shared.data.persons.mapper.toFlags
import com.nightcode.snout.shared.data.persons.mapper.toPerson
import com.nightcode.snout.shared.domain.data.persons.PersonRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.model.persons.PersonEventPage
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [PersonRepository::class])
internal class PersonRepositoryImpl(private val api: PersonsApi) : PersonRepository {

    override suspend fun getPersons(projectId: Long, query: String): Either<DataError, List<Person>> =
        api.getPersons(projectId, query).map { page -> page.results.map { it.toPerson() } }

    override suspend fun getPerson(projectId: Long, uuid: String): Either<DataError, Person> =
        api.getPerson(projectId, uuid).map { it.toPerson() }

    override suspend fun findPersonByDistinctId(projectId: Long, distinctId: String): Either<DataError, Person> =
        api.getPersonsByDistinctId(projectId, distinctId).flatMap { page ->
            page.results.firstOrNull()?.toPerson()?.right() ?: DataError.NotFound.left()
        }

    override suspend fun getEvents(
        projectId: Long,
        uuid: String,
        before: Instant?,
    ): Either<DataError, PersonEventPage> = api.getEvents(projectId, uuid, before?.toString()).map { it.toEventPage() }

    override suspend fun getFlags(projectId: Long, distinctId: String): Either<DataError, List<PersonFlag>> =
        api.getFlagEvaluations(projectId, distinctId).map { it.toFlags() }

    override suspend fun getExceptions(projectId: Long, uuid: String): Either<DataError, List<PersonException>> =
        api.getExceptions(projectId, uuid).map { it.toExceptions() }
}
