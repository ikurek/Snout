package com.nightcode.snout.shared.di

import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration

/**
 * Koin entry point that only exists for the compiler plugin's build-time graph check: every definition in
 * [SharedModule] must find all of its dependencies, so a repository whose data module isn't included fails the
 * build. The app's own entry point (composeApp) can't be checked this way because it doesn't see the data layer.
 */
@KoinApplication(modules = [SharedModule::class])
internal object SharedGraph

/** Never called: the plugin only checks graphs at a call site such as this one. */
@Suppress("unused")
internal fun sharedGraphConfiguration() = koinConfiguration<SharedGraph>()
