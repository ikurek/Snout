package com.nightcode.snout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val startupViewModel: StartupViewModel by viewModels()
    private var startup: Startup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // The launch screen stays up until the first screen has what it needs to draw.
        installSplashScreen().setKeepOnScreenCondition { startup == null }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // The home bottom bar paints its own color behind the gesture area; the system scrim would tint it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        // A recreated activity already handled its link; one reopened from Recents would replay it.
        val fromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        if (savedInstanceState == null && !fromHistory) handleLink(intent)
        // A recreated activity finds the startup already read, and sets its content at once.
        lifecycleScope.launch {
            val startup = startupViewModel.startup.filterNotNull().first()
            setContent { App(startup) }
            this@MainActivity.startup = startup
        }
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
        text?.let(::submitIncomingLink)
    }
}

/** Reads the [Startup] once per launch and keeps it across configuration changes. */
internal class StartupViewModel : ViewModel() {
    val startup: StateFlow<Startup?> = flow { emit(loadStartup()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
