package com.nightcode.snout.shared.api

import org.koin.core.annotation.Module

/** Provides the [AppBuild], which only the platform can tell. */
@Module
expect class PlatformApiModule()
