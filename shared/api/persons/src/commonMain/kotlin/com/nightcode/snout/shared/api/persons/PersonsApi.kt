package com.nightcode.snout.shared.api.persons

import com.nightcode.snout.shared.api.QueryRequestDto
import com.nightcode.snout.shared.api.networkCall
import com.nightcode.snout.shared.api.persons.model.FlagEvaluationDto
import com.nightcode.snout.shared.api.persons.model.HogQLQueryDto
import com.nightcode.snout.shared.api.persons.model.HogQLResultsDto
import com.nightcode.snout.shared.api.persons.model.PersonDto
import com.nightcode.snout.shared.api.persons.model.PersonsResponseDto
import com.nightcode.snout.shared.api.postJson
import com.nightcode.snout.shared.api.projectUrl
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.decodeURLPart
import org.koin.core.annotation.Single

private const val PersonsPageSize = 50

const val PersonEventsPageSize = 50

private const val PersonExceptionsLimit = 20

/**
 * How far back a person's events and exceptions are looked for: the events table is sorted by time, so an unbounded
 * lookup by person would read the project's whole history.
 */
private const val LookbackDays = 30

private const val PersonIdValue = "person_id"
private const val BeforeValue = "before"

private fun eventsQuery(paged: Boolean) = """
    SELECT uuid, event, timestamp, properties.${'$'}current_url, properties.${'$'}screen_name
    FROM events
    WHERE person_id = toUUID({$PersonIdValue})
        AND timestamp > now() - INTERVAL $LookbackDays DAY
        ${if (paged) "AND timestamp < toDateTime64({$BeforeValue}, 6, 'UTC')" else ""}
    ORDER BY timestamp DESC
    LIMIT $PersonEventsPageSize
""".trimIndent().lines().filter {
    it.isNotBlank()
}.joinToString("\n")

private val ExceptionsQuery = """
    SELECT uuid, timestamp, properties.${'$'}exception_issue_id, properties.${'$'}exception_types,
        properties.${'$'}exception_values
    FROM events
    WHERE event = '${'$'}exception'
        AND person_id = toUUID({$PersonIdValue})
        AND timestamp > now() - INTERVAL $LookbackDays DAY
    ORDER BY timestamp DESC
    LIMIT $PersonExceptionsLimit
""".trimIndent()

@Single
class PersonsApi(private val client: HttpClient, private val authDataStore: AuthDataStore) {

    suspend fun getPersons(projectId: Long, search: String?) = networkCall<PersonsResponseDto> {
        client.get(authDataStore.projectUrl(projectId, "persons/")) {
            search?.takeIf { it.isNotBlank() }?.let { parameter("search", it.trim()) }
            parameter("limit", PersonsPageSize)
        }.body()
    }

    /**
     * The person with exactly this distinct ID, as PostHog's own person links name them. [encodedDistinctId] comes
     * percent-encoded from a link's path.
     */
    suspend fun getPersonsByDistinctId(projectId: Long, encodedDistinctId: String) = networkCall<PersonsResponseDto> {
        client.get(authDataStore.projectUrl(projectId, "persons/")) {
            parameter("distinct_id", encodedDistinctId.decodeURLPart())
        }.body()
    }

    suspend fun getPerson(projectId: Long, uuid: String) = networkCall<PersonDto> {
        client.get(authDataStore.projectUrl(projectId, "persons/$uuid/")).body()
    }

    suspend fun getFlagEvaluations(projectId: Long, distinctId: String) = networkCall<Map<String, FlagEvaluationDto>> {
        client.get(authDataStore.projectUrl(projectId, "feature_flags/evaluation_reasons/")) {
            parameter("distinct_id", distinctId)
        }.body()
    }

    suspend fun getEvents(projectId: Long, personUuid: String, before: String?) = networkCall<HogQLResultsDto> {
        val values = buildMap {
            put(PersonIdValue, personUuid)
            before?.let { put(BeforeValue, it) }
        }
        query(projectId, HogQLQueryDto(eventsQuery(paged = before != null), values))
    }

    suspend fun getExceptions(projectId: Long, personUuid: String) = networkCall<HogQLResultsDto> {
        query(projectId, HogQLQueryDto(ExceptionsQuery, mapOf(PersonIdValue to personUuid)))
    }

    private suspend fun query(projectId: Long, query: HogQLQueryDto): HogQLResultsDto =
        client.postJson(authDataStore.projectUrl(projectId, "query/"), QueryRequestDto(query)).body()
}
