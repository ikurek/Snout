package com.nightcode.snout

import androidx.compose.runtime.Composable
import com.nightcode.snout.features.links.IncomingLinks

@Composable
@Suppress("FunctionNaming")
fun MainView(onReady: () -> Unit = {}, incomingLinks: IncomingLinks? = null) = App(onReady, incomingLinks)
