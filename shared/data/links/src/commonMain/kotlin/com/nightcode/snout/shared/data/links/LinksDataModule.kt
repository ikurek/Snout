package com.nightcode.snout.shared.data.links

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [PlatformLinksDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.links")
class LinksDataModule

@Module
expect class PlatformLinksDataModule()
