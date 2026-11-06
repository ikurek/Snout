package com.nightcode.snout.features.persons

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.model.persons.TestPersons

internal data class PersonsUiState(
    val persons: List<Person>? = null,
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val query: String = "",
    val searchResults: List<Person>? = null,
    val searchError: DataError? = null,
)

@TestData
@OptIn(TestData::class)
internal val TestPersonsUiState = PersonsUiState(persons = TestPersons)
