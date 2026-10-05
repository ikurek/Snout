package com.nightcode.snout.features.annotations

import com.nightcode.snout.common.state.CachedUiState
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.annotations.TestAnnotation
import com.nightcode.snout.shared.domain.model.common.DataError

internal sealed interface CreateStatus {
    data object Idle : CreateStatus

    data object Saving : CreateStatus

    data class Failed(val error: DataError) : CreateStatus
}

internal data class AnnotationsUiState(
    val annotations: CachedUiState<List<Annotation>> = CachedUiState.Loading,
    val create: CreateStatus = CreateStatus.Idle,
)

@TestData
@OptIn(TestData::class)
internal val TestAnnotationsUiState = AnnotationsUiState(
    annotations = CachedUiState.Content(
        listOf(TestAnnotation, TestAnnotation.copy(id = 6, content = "Marketing campaign started", createdBy = null)),
        isRefreshing = false,
    ),
)
