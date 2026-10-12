package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Returns a function that opens any [PostHogResource] in the web app of the signed-in instance: the one way every
 * screen's "Open in PostHog" action gets there.
 */
@Composable
internal fun rememberPostHogOpener(): (PostHogResource) -> Unit {
    val getPostHogUrl = koinInject<GetPostHogUrl>()
    val openInBrowser = koinInject<OpenInBrowser>()
    val scope = rememberCoroutineScope()
    return remember(getPostHogUrl, openInBrowser, scope) {
        { resource -> scope.launch { getPostHogUrl(resource)?.let(openInBrowser::invoke) } }
    }
}
