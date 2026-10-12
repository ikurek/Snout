package com.nightcode.snout.shared.data.annotations

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.api.annotations.AnnotationsApi
import com.nightcode.snout.shared.api.annotations.model.CreateAnnotationRequestDto
import com.nightcode.snout.shared.data.annotations.mapper.toAnnotation
import com.nightcode.snout.shared.data.common.ChangeSignal
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [AnnotationRepository::class])
internal class AnnotationRepositoryImpl(private val api: AnnotationsApi) : AnnotationRepository {

    private val changeSignal = ChangeSignal()
    override val changes: Flow<Unit> = changeSignal.changes

    override suspend fun getAnnotations(projectId: Long): Either<DataError, List<Annotation>> =
        api.getAnnotations(projectId)
            .mapLeft { it.toDataError() }
            .map { response ->
                response.results
                    .filterNot { it.deleted }
                    .mapNotNull { it.toAnnotation() }
                    .sortedByDescending { it.dateMarker }
            }

    override suspend fun createAnnotation(
        projectId: Long,
        content: String,
        dateMarker: Instant,
    ): Either<DataError, Annotation> = either {
        val created = api.createAnnotation(
            projectId,
            CreateAnnotationRequestDto(content = content, dateMarker = dateMarker),
        )
            .mapLeft { it.toDataError() }
            .bind()
        ensureNotNull(created.toAnnotation()) { DataError.UnexpectedError }
    }.onRight { changeSignal.emit() }
}
