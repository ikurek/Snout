package com.nightcode.snout.shared.domain.usecase.annotations

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import org.koin.core.annotation.Single

@Single
class RefreshAnnotations(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnnotationRepository,
) {
    suspend operator fun invoke(): Either<DataError, Unit> = repository.refreshAnnotations(getSelectedProjectId())
}
