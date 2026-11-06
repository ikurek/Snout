package com.nightcode.snout.features.persons.detail

import com.nightcode.snout.designsystem.organisms.LoadMoreStatus
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.model.persons.PersonEvent
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.model.persons.TestPerson
import com.nightcode.snout.shared.domain.model.persons.TestPersonEvents
import com.nightcode.snout.shared.domain.model.persons.TestPersonExceptions
import com.nightcode.snout.shared.domain.model.persons.TestPersonFlags

internal enum class PersonTab {
    Properties,
    Events,
    Flags,
    Errors,
}

internal data class PersonDetailUiState(
    val person: Person? = null,
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val tab: PersonTab = PersonTab.Properties,
    val events: List<PersonEvent>? = null,
    val eventsError: DataError? = null,
    val eventsLoadMore: LoadMoreStatus = LoadMoreStatus.Done,
    val flags: List<PersonFlag>? = null,
    val flagsError: DataError? = null,
    val exceptions: List<PersonException>? = null,
    val exceptionsError: DataError? = null,
)

@TestData
@OptIn(TestData::class)
internal val TestPersonDetailUiState = PersonDetailUiState(
    person = TestPerson,
    events = TestPersonEvents,
    eventsLoadMore = LoadMoreStatus.Idle,
    flags = TestPersonFlags,
    exceptions = TestPersonExceptions,
)
