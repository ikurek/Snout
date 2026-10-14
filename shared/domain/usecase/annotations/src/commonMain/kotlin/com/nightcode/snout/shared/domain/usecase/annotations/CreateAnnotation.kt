package com.nightcode.snout.shared.domain.usecase.annotations

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single
import kotlin.time.Instant

/** Why [CreateAnnotation] created nothing: content it refused before sending, or PostHog's answer. */
sealed interface CreateAnnotationError {
    /** The content is empty or only whitespace; nothing was sent. */
    data object ContentIsBlank : CreateAnnotationError

    data class Failed(val error: DataError) : CreateAnnotationError
}

@Single
class CreateAnnotation(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    suspend operator fun invoke(content: String, dateMarker: Instant): Either<CreateAnnotationError, Annotation> =
        either {
            val trimmed = content.trim()
            ensure(trimmed.isNotEmpty()) { CreateAnnotationError.ContentIsBlank }
            repository.createAnnotation(getSelectedProjectId(), trimmed, dateMarker)
                .mapLeft(CreateAnnotationError::Failed)
                .bind()
        }
}
