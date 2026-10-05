package com.nightcode.snout.shared.data.annotations

import com.nightcode.snout.shared.api.annotations.AnnotationsApi
import com.nightcode.snout.shared.api.annotations.AnnotationsApiModule
import com.nightcode.snout.shared.data.common.DataCommonModule
import com.nightcode.snout.shared.data.common.JsonCacheStore
import com.nightcode.snout.shared.domain.data.annotations.AnnotationRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AnnotationsApiModule::class, DataCommonModule::class])
class AnnotationsDataModule {
    @Single(binds = [AnnotationRepository::class])
    fun provideAnnotationRepository(api: AnnotationsApi, cache: JsonCacheStore): AnnotationRepository =
        AnnotationRepositoryImpl(api, cache)
}
