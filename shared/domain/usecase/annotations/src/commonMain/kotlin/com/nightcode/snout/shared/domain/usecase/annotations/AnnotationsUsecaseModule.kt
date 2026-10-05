package com.nightcode.snout.shared.domain.usecase.annotations

import com.nightcode.snout.shared.data.annotations.AnnotationsDataModule
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveSelectedProjectId
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AnnotationsDataModule::class, AppStateUsecaseModule::class])
class AnnotationsUsecaseModule {
    @Single
    fun observeAnnotations(observe: ObserveSelectedProjectId, repository: AnnotationRepository) =
        ObserveAnnotations(observe, repository)

    @Single
    fun refreshAnnotations(get: GetSelectedProjectId, repository: AnnotationRepository) =
        RefreshAnnotations(get, repository)

    @Single
    fun createAnnotation(get: GetSelectedProjectId, repository: AnnotationRepository) = CreateAnnotation(
        get,
        repository,
    )
}
