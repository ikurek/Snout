package com.nightcode.snout.features.annotations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotation
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotationError
import com.nightcode.snout.shared.domain.usecase.annotations.GetAnnotations
import com.nightcode.snout.shared.domain.usecase.annotations.ObserveAnnotationReloads
import com.nightcode.snout.shared.domain.usecase.appstate.ProjectReload
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
internal class AnnotationsViewModel(
    private val getAnnotations: GetAnnotations,
    private val createAnnotation: CreateAnnotation,
    observeAnnotationReloads: ObserveAnnotationReloads,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnotationsUiState())
    val uiState: StateFlow<AnnotationsUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<AnnotationsSideEffect>()
    val sideEffects: Flow<AnnotationsSideEffect> = _sideEffects.receiveAsFlow()

    private var lastReload: ProjectReload? = null
    private var loadJob: Job? = null
    private var hasEntered = false

    init {
        viewModelScope.launch {
            // A created annotation reloads the list through the annotation reloads.
            observeAnnotationReloads().collect { reload ->
                // Another project's annotations start from the skeleton; the same project's stay on screen meanwhile.
                if (reload != lastReload) _uiState.update { it.copy(annotations = null) }
                lastReload = reload
                load()
            }
        }
    }

    fun refresh() = load()

    /** Refreshes each time the screen is entered again, but not the first time nor while a load runs. */
    fun onScreenEntered() {
        if (hasEntered && loadJob?.isActive != true) refresh()
        hasEntered = true
    }

    fun create(content: String, dateMarker: Instant) = viewModelScope.launch {
        _uiState.update { it.copy(create = CreateStatus.Saving) }
        createAnnotation(content, dateMarker).fold(
            ifLeft = { error ->
                val status = when (error) {
                    // The sheet only enables saving for non-blank content, so there is nothing to tell.
                    CreateAnnotationError.ContentIsBlank -> CreateStatus.Idle

                    is CreateAnnotationError.Failed -> CreateStatus.Failed(error.error)
                }
                _uiState.update { it.copy(create = status) }
            },
            ifRight = {
                _uiState.update { it.copy(create = CreateStatus.Idle) }
                _sideEffects.send(AnnotationsSideEffect.Created)
            },
        )
    }

    fun onCreateErrorDismissed() {
        _uiState.update { it.copy(create = CreateStatus.Idle) }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.annotations != null, error = null) }
            val result = getAnnotations()
            _uiState.update { state ->
                result.fold(
                    ifLeft = { state.copy(isRefreshing = false, error = it) },
                    ifRight = { state.copy(annotations = it, isRefreshing = false, error = null) },
                )
            }
        }
    }
}
