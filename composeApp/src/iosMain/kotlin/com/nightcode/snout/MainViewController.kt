package com.nightcode.snout

import androidx.compose.ui.window.ComposeUIViewController
import com.nightcode.snout.features.links.IncomingLinks

@Suppress("FunctionNaming")
fun MainViewController(incomingLinks: IncomingLinks) = ComposeUIViewController { App(incomingLinks = incomingLinks) }
