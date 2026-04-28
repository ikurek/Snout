package com.nightcode.snout.features.home

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel : ViewModel() {

    val greeting: String = "Hello!"
}
