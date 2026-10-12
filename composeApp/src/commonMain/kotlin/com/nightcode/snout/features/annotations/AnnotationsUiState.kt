package com.nightcode.snout.features.annotations

import com.nightcode.snout.common.state.RefreshableUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.annotations.TestAnnotation
import com.nightcode.snout.shared.domain.model.common.DataError

/** Saving from the create sheet; a failure stays in the sheet until it is dismissed. */
internal sealed interface CreateStatus {
    data object Idle : CreateStatus

    data object Saving : CreateStatus

    data class Failed(val error: DataError) : CreateStatus
}

internal sealed interface AnnotationsSideEffect {
    data object Created : AnnotationsSideEffect
}

internal data class AnnotationsUiState(
    val annotations: RefreshableUiState<List<Annotation>> = RefreshableUiState.Loading,
    val create: CreateStatus = CreateStatus.Idle,
)

@TestData
@OptIn(TestData::class)
internal val TestAnnotationsUiState = AnnotationsUiState(
    annotations = RefreshableUiState.Content(
        listOf(TestAnnotation, TestAnnotation.copy(id = 6, content = "Marketing campaign started", createdBy = null)),
        isRefreshing = false,
    ),
)
