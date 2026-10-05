package com.nightcode.snout.shared.domain.data.annotations

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface AnnotationRepository {
    fun observeAnnotations(projectId: Long): Flow<List<Annotation>?>
    suspend fun refreshAnnotations(projectId: Long): Either<DataError, Unit>
    suspend fun createAnnotation(projectId: Long, content: String, dateMarker: Instant): Either<DataError, Annotation>
}
