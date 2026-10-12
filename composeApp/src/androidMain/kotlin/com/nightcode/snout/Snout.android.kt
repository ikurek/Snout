package com.nightcode.snout

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/** Starts the app's graph; call from [Application.onCreate], so it's up before any activity or link arrives. */
fun startSnout(application: Application) = startSnoutKoin {
    androidLogger()
    androidContext(application)
}
