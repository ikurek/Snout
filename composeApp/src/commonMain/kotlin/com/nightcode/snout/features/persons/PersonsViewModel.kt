package com.nightcode.snout.features.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveProjectReloads
import com.nightcode.snout.shared.domain.usecase.persons.GetPersons
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@KoinViewModel
internal class PersonsViewModel(
    private val getPersons: GetPersons,
    observeProjectReloads: ObserveProjectReloads,
    searchDebounce: Duration = 300.milliseconds,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonsUiState())
    val uiState: StateFlow<PersonsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            observeProjectReloads().collect { reload ->
                if (reload.projectChanged) {
                    _uiState.update { it.copy(persons = null, searchResults = null) }
                    val query = _uiState.value.query
                    if (query.isNotBlank()) viewModelScope.launch { search(query) }
                }
                load()
            }
        }
        viewModelScope.launch {
            _uiState.map { it.query }.distinctUntilChanged()
                .debounce(searchDebounce).distinctUntilChanged()
                .collectLatest { search(it) }
        }
    }

    fun onQueryChanged(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun refresh() {
        load()
        val query = _uiState.value.query
        if (query.isNotBlank()) viewModelScope.launch { search(query, keepResults = true) }
    }

    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.persons != null, error = null) }
            val result = getPersons()
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(persons = it, isRefreshing = false, error = null) },
                )
            }
        }
    }

    private suspend fun search(query: String, keepResults: Boolean = false) {
        _uiState.update { it.copy(searchResults = it.searchResults.takeIf { keepResults }, searchError = null) }
        if (query.isBlank()) return
        val result = getPersons(query)
        _uiState.update { state ->
            if (state.query != query) return@update state
            result.fold(
                ifLeft = { state.copy(searchError = it) },
                ifRight = { state.copy(searchResults = it) },
            )
        }
    }
}
