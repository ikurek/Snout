package com.nightcode.snout.shared.domain.usecase.annotations

import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveAnnotations(
    private val observeSelectedProjectId: ObserveSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    operator fun invoke(): Flow<List<Annotation>?> =
        observeSelectedProjectId.flatMapProject { repository.observeAnnotations(it) }
}
