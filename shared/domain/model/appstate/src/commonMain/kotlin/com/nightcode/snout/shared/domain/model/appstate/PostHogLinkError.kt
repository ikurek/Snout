package com.nightcode.snout.shared.domain.model.appstate

/** Why a link couldn't be read as a PostHog page. */
enum class PostHogLinkError {
    /** Not an https link to a project (`https://{host}/project/{id}/…`). */
    NotAProjectLink,

    /** A project page Snout has no screen for: replays, persons, "new" forms… */
    UnsupportedPage,
}
