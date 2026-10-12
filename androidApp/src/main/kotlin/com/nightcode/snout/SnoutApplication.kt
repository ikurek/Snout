package com.nightcode.snout

import android.app.Application

class SnoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startSnout(this)
    }
}
