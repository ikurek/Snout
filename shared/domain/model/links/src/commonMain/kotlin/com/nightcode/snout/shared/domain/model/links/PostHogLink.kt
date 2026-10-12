package com.nightcode.snout.shared.domain.model.links

/** A page of the PostHog web app, read from a link: which instance, which project and what it shows. */
data class PostHogLink(val instanceUrl: String, val projectId: Long, val resource: PostHogResource)
