package com.nightcode.snout.shared.domain.usecase.annotations

import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AnnotationsUsecaseModule {
    @Single
    fun provideObserveAnnotations(observe: ObserveSelectedProjectId, repository: AnnotationRepository) =
        ObserveAnnotations(observe, repository)

    @Single
    fun provideRefreshAnnotations(get: GetSelectedProjectId, repository: AnnotationRepository) =
        RefreshAnnotations(get, repository)

    @Single
    fun provideCreateAnnotation(get: GetSelectedProjectId, repository: AnnotationRepository) = CreateAnnotation(
        get,
        repository,
    )
}
