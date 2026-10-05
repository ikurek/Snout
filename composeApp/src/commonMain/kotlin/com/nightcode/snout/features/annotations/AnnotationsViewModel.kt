package com.nightcode.snout.features.annotations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightcode.snout.common.state.RefreshController
import com.nightcode.snout.common.state.listUiState
import com.nightcode.snout.shared.domain.usecase.annotations.CreateAnnotation
import com.nightcode.snout.shared.domain.usecase.annotations.ObserveAnnotations
import com.nightcode.snout.shared.domain.usecase.annotations.RefreshAnnotations
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

internal sealed interface AnnotationsSideEffect {
    data object Created : AnnotationsSideEffect
}

@KoinViewModel
internal class AnnotationsViewModel(
    private val refreshAnnotations: RefreshAnnotations,
    private val createAnnotation: CreateAnnotation,
    observeAnnotations: ObserveAnnotations,
    observeSelectedProjectId: ObserveSelectedProjectId,
) : ViewModel() {

    private val refresh = RefreshController()
    private val create = MutableStateFlow<CreateStatus>(CreateStatus.Idle)
    private val _sideEffects = Channel<AnnotationsSideEffect>()
    val sideEffects: Flow<AnnotationsSideEffect> = _sideEffects.receiveAsFlow()

    val uiState: StateFlow<AnnotationsUiState> = combine(
        observeAnnotations(),
        refresh.status,
        create,
    ) { items, status, create ->
        AnnotationsUiState(annotations = listUiState(items, status), create = create)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnnotationsUiState())

    init {
        viewModelScope.launch {
            observeSelectedProjectId().collectLatest { refresh.run { refreshAnnotations() } }
        }
    }

    fun refresh() = viewModelScope.launch { refresh.run { refreshAnnotations() } }

    fun onCreate(content: String, dateMarker: Instant) = viewModelScope.launch {
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

    fun dismissCreateError() {
        create.value = CreateStatus.Idle
    }
}
