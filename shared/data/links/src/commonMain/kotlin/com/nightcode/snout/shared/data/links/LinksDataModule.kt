package com.nightcode.snout.shared.data.links

import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformLinksDataModule::class])
class LinksDataModule {
    @Single(binds = [IncomingLinkRepository::class])
    fun provideIncomingLinkRepository(): IncomingLinkRepository = IncomingLinkRepositoryImpl()
}

@Module
expect class PlatformLinksDataModule()
