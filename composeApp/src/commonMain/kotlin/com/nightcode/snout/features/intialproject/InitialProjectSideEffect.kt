package com.nightcode.snout.features.intialproject

internal sealed interface InitialProjectSideEffect {
    data object ProjectSelected : InitialProjectSideEffect
}
