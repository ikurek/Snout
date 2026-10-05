package com.nightcode.snout.shared.domain.usecase.annotations

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAnnotations(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    operator fun invoke(): Flow<List<Annotation>?> =
        observeSelectedProjectId().flatMapLatest { repository.observeAnnotations(it) }
}

class RefreshAnnotations(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshAnnotations(getSelectedProjectId())
}

class CreateAnnotation(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    suspend operator fun invoke(content: String, dateMarker: Instant): Either<DataError, Annotation> =
        repository.createAnnotation(getSelectedProjectId(), content.trim(), dateMarker)
}
