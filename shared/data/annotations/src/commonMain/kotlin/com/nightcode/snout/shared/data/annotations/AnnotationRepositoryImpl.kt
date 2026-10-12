package com.nightcode.snout.shared.data.annotations

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.nightcode.snout.shared.api.annotations.AnnotationsApi
import com.nightcode.snout.shared.api.annotations.model.AnnotationDto
import com.nightcode.snout.shared.api.annotations.model.CreateAnnotationRequestDto
import com.nightcode.snout.shared.data.annotations.mapper.toAnnotation
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.data.common.toDataError
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [AnnotationRepository::class])
internal class AnnotationRepositoryImpl(private val api: AnnotationsApi, private val cache: JsonCacheStore) :
    AnnotationRepository {

    override fun observeAnnotations(projectId: Long): Flow<List<Annotation>?> = cache.observe(key(projectId))

    override suspend fun refreshAnnotations(projectId: Long): Either<DataError, Unit> = api.getAnnotations(projectId)
        .mapLeft { it.toDataError() }
        .map { response -> cache.put(key(projectId), response.results.toSortedAnnotations()) }

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
        val annotation = ensureNotNull(created.toAnnotation()) { DataError.UnexpectedError }
        val cached = cache.observe<List<Annotation>>(key(projectId)).first().orEmpty()
        cache.put(key(projectId), (cached + annotation).sortedByDescending { it.dateMarker })
        annotation
    }

    private fun List<AnnotationDto>.toSortedAnnotations() =
        filterNot { it.deleted }.mapNotNull { it.toAnnotation() }.sortedByDescending { it.dateMarker }

    private fun key(projectId: Long) = "annotations:$projectId"
}
