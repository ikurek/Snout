package com.nightcode.snout.features.annotations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.loadRefreshableList
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotation
import com.nightcode.snout.shared.domain.usecase.annotations.GetAnnotations
import com.nightcode.snout.shared.domain.usecase.annotations.ObserveAnnotationReloads
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
internal class AnnotationsViewModel(
    private val createAnnotation: CreateAnnotation,
    getAnnotations: GetAnnotations,
    observeAnnotationReloads: ObserveAnnotationReloads,
) : ViewModel() {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val create = MutableStateFlow<CreateStatus>(CreateStatus.Idle)
    private val _sideEffects = Channel<AnnotationsSideEffect>()
    val sideEffects: Flow<AnnotationsSideEffect> = _sideEffects.receiveAsFlow()

    // A created annotation reloads the list through the annotation reloads.
    val uiState: StateFlow<AnnotationsUiState> = combine(
        observeAnnotationReloads().loadRefreshableList(viewModelScope, refreshes) { getAnnotations() },
        create,
    ) { annotations, create ->
        AnnotationsUiState(annotations = annotations, create = create)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnnotationsUiState())

    fun refresh() {
        refreshes.tryEmit(Unit)
    }

    fun create(content: String, dateMarker: Instant) = viewModelScope.launch {
        if (content.isBlank()) return@launch
        create.value = CreateStatus.Saving
        createAnnotation(content, dateMarker).fold(
            ifLeft = { create.value = CreateStatus.Failed(it) },
            ifRight = {
                create.value = CreateStatus.Idle
                _sideEffects.send(AnnotationsSideEffect.Created)
            },
        )
    }

    fun onCreateErrorDismissed() {
        create.value = CreateStatus.Idle
    }
}
