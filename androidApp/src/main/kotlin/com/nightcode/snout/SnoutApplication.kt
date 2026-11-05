package com.nightcode.snout

import android.app.Application
import com.nightcode.snout.appwidget.startWidgets

class SnoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startSnout(this)
        startWidgets(this)
    }
}
