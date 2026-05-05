package com.nightcode.snout.shared.common.coroutines

import org.koin.core.annotation.Qualifier

@Qualifier
annotation class IoDispatcher

@Qualifier
annotation class MainDispatcher

@Qualifier
annotation class DefaultDispatcher
