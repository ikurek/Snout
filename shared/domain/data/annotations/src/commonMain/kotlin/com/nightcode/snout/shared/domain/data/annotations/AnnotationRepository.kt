package com.nightcode.snout.shared.domain.data.annotations

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface AnnotationRepository {
    /** Emits after each successful change made through this app, so lists showing the old state load again. */
    val changes: Flow<Unit>

    suspend fun getAnnotations(projectId: Long): Either<DataError, List<Annotation>>
    suspend fun createAnnotation(projectId: Long, content: String, dateMarker: Instant): Either<DataError, Annotation>
}
