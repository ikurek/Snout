package com.nightcode.snout.widgets

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** The widget loaders; the app's graph includes it on Android, the extension's graph on iOS. */
@Module
@ComponentScan("com.nightcode.snout.widgets")
class WidgetsModule
