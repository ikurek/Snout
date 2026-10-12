package com.nightcode.snout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nightcode.snout.features.links.IncomingLinks

class MainActivity : ComponentActivity() {
    private val incomingLinks = IncomingLinks()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // The home bottom bar paints its own color behind the gesture area; the system scrim would tint it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        keepSplashUntilReady()
        // A recreated activity already handled its link; one reopened from Recents would replay it.
        val fromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        if (savedInstanceState == null && !fromHistory) handleLink(intent)
        setContent { MainView(onReady = { ready = true }, incomingLinks = incomingLinks) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    /** A PostHog link opened from another app, or text shared to Snout that contains one. */
    private fun handleLink(intent: Intent) {
        val text = when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
        text?.let(incomingLinks::open)
    }

    private var ready = false

    /** Holds the first draw, and with it the system splash, until the app knows which screen to open. */
    private fun keepSplashUntilReady() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (ready) content.viewTreeObserver.removeOnPreDrawListener(this)
                    return ready
                }
            },
        )
    }
}
