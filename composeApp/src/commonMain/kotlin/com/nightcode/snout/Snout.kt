package com.nightcode.snout

import com.nightcode.snout.shared.domain.usecase.appstate.ObserveWidgetChanges
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import com.nightcode.snout.shared.domain.usecase.telemetry.ApplyTelemetrySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

/*
 * The entry points the platform hosts call: start the app's graph once per process, before any UI, then hand over
 * the links the system asks the app to open, and redraw home-screen widgets when what they show changes.
 */

/** Every definition of the app, the one module list both platforms start and AppModuleTest verifies. */
internal fun snoutModule(): Module = AppModule().module()

/** [platformSetup] adds what only the platform has, such as Android's application context. */
internal fun startSnoutKoin(platformSetup: KoinAppDeclaration) {
    startKoin {
        platformSetup()
        modules(snoutModule())
    }
}

/**
 * Starts reporting as the person last chose (see Settings > Privacy). The platforms run it right after the graph,
 * blocking for the one local read it takes, so PostHog starts before the app's first open is captured.
 */
internal suspend fun applyTelemetrySettings() = KoinPlatform.getKoin().get<ApplyTelemetrySettings>().invoke()

/** Main-thread work that outlives any screen; holding the link takes no time, so nothing ever waits on it. */
private val HostScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

/**
 * Queues a link the platform was asked to open: an opened URL, or shared text with a link somewhere in it. It stays
 * pending until Home can open it, so a link that arrives during a cold start or onboarding isn't lost. Call on the
 * main thread, after the platform started the graph.
 */
fun submitIncomingLink(text: String) {
    val submitIncomingLink = KoinPlatform.getKoin().get<SubmitIncomingLink>()
    HostScope.launch { submitIncomingLink(text) }
}

/**
 * Calls [onChange] on the main thread each time the person signs in or out, the one change every home-screen widget
 * shows (each names its own project and range, whatever the app shows). The platform reloads its widgets there
 * (`startWidgets`, WidgetKit's `reloadAllTimelines`), so a widget never shows the previous account until its next
 * refresh. Call once, after the platform started the graph.
 */
fun observeWidgetChanges(onChange: () -> Unit) {
    val observeWidgetChanges = KoinPlatform.getKoin().get<ObserveWidgetChanges>()
    // Reading the key off the main thread: iOS blocks it for the startup read, which waits on the same key.
    HostScope.launch {
        observeWidgetChanges().flowOn(Dispatchers.Default)
            // A key the secure store can't read (the Keystore failing) only stops these redraws: the widgets still
            // refresh on their own and show the failure there, instead of the app crashing at start.
            .catch { }
            .collect { onChange() }
    }
}
