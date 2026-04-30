package com.nightcode.snout.features.intialproject

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Serializable
internal data class InitialProjectNavKey(val firstName: String, val dateJoined: Instant) : NavKey

fun NavBackStack<NavKey>.toInitialProject(firstName: String, dateJoined: Instant) =
    add(InitialProjectNavKey(firstName, dateJoined))

@Composable
internal fun InitialProjectRoute(key: InitialProjectNavKey) {
    val viewModel = koinViewModel<InitialProjectViewModel>()

    InitialProjectScreen(
        firstName = key.firstName,
        dateJoined = key.dateJoined,
    )
}
