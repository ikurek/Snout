package com.nightcode.snout.features.annotations

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
    /** The project's annotations; null until the first load succeeds. */
    val annotations: List<Annotation>? = null,
    /** Why the last load failed; a banner over [annotations] once they're loaded. */
    val error: DataError? = null,
    val isRefreshing: Boolean = false,
    val create: CreateStatus = CreateStatus.Idle,
)

@TestData
@OptIn(TestData::class)
internal val TestAnnotationsUiState = AnnotationsUiState(
    annotations = listOf(
        TestAnnotation,
        TestAnnotation.copy(id = 6, content = "Marketing campaign started", createdBy = null),
    ),
)
