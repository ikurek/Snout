package com.nightcode.snout

import androidx.compose.ui.window.ComposeUIViewController
import com.nightcode.snout.navigation.MainNavGraph

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { MainNavGraph() }
