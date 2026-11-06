package com.nightcode.snout.features.persons.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.designsystem.organisms.LoadMoreStatus
import com.nightcode.snout.navigation.PersonDetailNavKey
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.usecase.persons.GetPerson
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonEvents
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonExceptions
import com.nightcode.snout.shared.domain.usecase.persons.GetPersonFlags
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
internal class PersonDetailViewModel(
    @InjectedParam private val navKey: PersonDetailNavKey,
    private val getPerson: GetPerson,
    private val getEvents: GetPersonEvents,
    private val getFlags: GetPersonFlags,
    private val getExceptions: GetPersonExceptions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private val tabJobs = mutableMapOf<PersonTab, Job>()
    private var loadMoreJob: Job? = null

    private var eventsBefore: Instant? = null
    private var hasEntered = false

    init {
        load(isRefresh = false)
    }

    fun onTabSelected(tab: PersonTab) {
        _uiState.update { it.copy(tab = tab) }
        val person = _uiState.value.person ?: return
        if (!_uiState.value.hasLoaded(tab) && tabJobs[tab]?.isActive != true) {
            tabJobs[tab] = viewModelScope.launch { loadTab(tab, person) }
        }
    }

    fun refresh() = load(isRefresh = true)

    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun onEventsEndReached() {
        val state = _uiState.value
        val before = eventsBefore
        val loaded = state.events?.takeIf { it.isNotEmpty() } ?: return
        val isLoading = state.eventsLoadMore == LoadMoreStatus.Loading || loadMoreJob?.isActive == true
        // The first page reloading replaces what is shown, so a page continuing it would be lost.
        val isReloading = loadJob?.isActive == true || tabJobs[PersonTab.Events]?.isActive == true
        if (before == null || isLoading || isReloading) return
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(eventsLoadMore = LoadMoreStatus.Loading) }
            getEvents(navKey.uuid, before).fold(
                ifLeft = { error -> _uiState.update { it.copy(eventsLoadMore = LoadMoreStatus.Failed(error)) } },
                ifRight = { page ->
                    eventsBefore = page.nextBefore
                    // Events sharing the cut-off's timestamp could come twice; each one shows once.
                    val known = loaded.mapTo(mutableSetOf()) { it.id }
                    _uiState.update {
                        it.copy(
                            events = loaded + page.events.filterNot { event -> event.id in known },
                            eventsLoadMore = page.nextBefore.toLoadMoreStatus(),
                        )
                    }
                },
            )
        }
    }

    private fun load(isRefresh: Boolean) {
        loadJob?.cancel()
        tabJobs.values.forEach { it.cancel() }
        tabJobs.clear()
        loadMoreJob?.cancel()
        _uiState.update { state ->
            state.forgetTabsExcept(state.tab.takeIf { isRefresh })
                .copy(isRefreshing = state.person != null, error = null)
        }
        loadJob = viewModelScope.launch {
            getPerson(navKey.uuid).fold(
                ifLeft = { error -> _uiState.update { it.copy(isRefreshing = false, error = error) } },
                ifRight = { person ->
                    _uiState.update { it.copy(person = person, error = null) }
                    loadTab(_uiState.value.tab, person)
                    _uiState.update { it.copy(isRefreshing = false) }
                },
            )
        }
    }

    private suspend fun loadTab(tab: PersonTab, person: Person) {
        when (tab) {
            PersonTab.Properties -> Unit

            PersonTab.Events -> {
                _uiState.update { it.copy(eventsError = null) }
                getEvents(person.uuid).fold(
                    ifLeft = { error -> _uiState.update { it.copy(eventsError = error) } },
                    ifRight = { page ->
                        eventsBefore = page.nextBefore
                        _uiState.update {
                            it.copy(events = page.events, eventsLoadMore = page.nextBefore.toLoadMoreStatus())
                        }
                    },
                )
            }

            PersonTab.Flags -> {
                _uiState.update { it.copy(flagsError = null) }
                val distinctId = person.primaryDistinctId
                if (distinctId == null) {
                    _uiState.update { it.copy(flags = emptyList()) }
                } else {
                    getFlags(distinctId).fold(
                        ifLeft = { error -> _uiState.update { it.copy(flagsError = error) } },
                        ifRight = { flags -> _uiState.update { it.copy(flags = flags) } },
                    )
                }
            }

            PersonTab.Errors -> {
                _uiState.update { it.copy(exceptionsError = null) }
                getExceptions(person.uuid).fold(
                    ifLeft = { error -> _uiState.update { it.copy(exceptionsError = error) } },
                    ifRight = { exceptions -> _uiState.update { it.copy(exceptions = exceptions) } },
                )
            }
        }
    }

    private fun Instant?.toLoadMoreStatus() = if (this == null) LoadMoreStatus.Done else LoadMoreStatus.Idle
}

private fun PersonDetailUiState.hasLoaded(tab: PersonTab): Boolean = when (tab) {
    PersonTab.Properties -> true
    PersonTab.Events -> events != null
    PersonTab.Flags -> flags != null
    PersonTab.Errors -> exceptions != null
}

private fun PersonDetailUiState.forgetTabsExcept(kept: PersonTab?): PersonDetailUiState = copy(
    events = events.takeIf { kept == PersonTab.Events },
    eventsError = null,
    eventsLoadMore = eventsLoadMore.takeIf { kept == PersonTab.Events } ?: LoadMoreStatus.Done,
    flags = flags.takeIf { kept == PersonTab.Flags },
    flagsError = null,
    exceptions = exceptions.takeIf { kept == PersonTab.Errors },
    exceptionsError = null,
)
