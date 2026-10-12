package com.nightcode.snout.shared.domain.model.links

/** Where an incoming link leads. */
sealed interface LinkResolution {
    data class InApp(val destination: DeepLinkDestination) : LinkResolution

    /** A page Snout can't show, or one on another instance or project. */
    data class Browser(val url: String) : LinkResolution
}
