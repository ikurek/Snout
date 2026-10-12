package com.nightcode.snout

import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

/*
 * The entry points the platform hosts call: start the app's graph once per process, before any UI, then hand over
 * the links the system asks the app to open.
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
