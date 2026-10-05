package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import com.nightcode.snout.shared.domain.usecase.appstate.GetPostHogUrl
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Returns a click handler that opens [resource] in the PostHog web app of the signed-in instance. */
@Composable
internal fun rememberOpenInPostHog(resource: PostHogResource): () -> Unit {
    val getPostHogUrl = koinInject<GetPostHogUrl>()
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    return {
        scope.launch { getPostHogUrl(resource)?.let(uriHandler::openUri) }
    }
}
