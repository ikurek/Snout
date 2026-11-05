package com.nightcode.snout.shared.di

import com.nightcode.snout.shared.core.CoreModule
import com.nightcode.snout.shared.telemetry.sdk.TelemetrySdkModule
import org.koin.core.annotation.Module

/** Composition root of the shared layer for the app: the whole [CoreModule], reporting through PostHog. */
@Module(includes = [CoreModule::class, TelemetrySdkModule::class])
class SharedModule
