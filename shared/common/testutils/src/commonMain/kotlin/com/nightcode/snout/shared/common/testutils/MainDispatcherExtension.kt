package com.nightcode.snout.shared.common.testutils

import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/** Replaces [Dispatchers.Main] with an unconfined test dispatcher so ViewModels can run in host tests. */
@OptIn(ExperimentalCoroutinesApi::class)
object MainDispatcherExtension : BeforeSpecListener, AfterSpecListener {
    override suspend fun beforeSpec(spec: Spec) = Dispatchers.setMain(UnconfinedTestDispatcher())

    override suspend fun afterSpec(spec: Spec) = Dispatchers.resetMain()
}
