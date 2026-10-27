package com.nightcode.snout

import android.app.Application
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Starts the app's graph and reporting; call from [Application.onCreate], so both are up before any activity or link
 * arrives. Reporting needs one local read (the person's choice), which takes milliseconds.
 */
fun startSnout(application: Application) {
    startSnoutKoin {
        androidLogger()
        androidContext(application)
    }
    runBlocking { applyTelemetrySettings() }
}
