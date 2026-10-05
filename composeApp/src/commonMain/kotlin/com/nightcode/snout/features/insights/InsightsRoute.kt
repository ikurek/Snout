package com.nightcode.snout.features.insights

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder

@Composable
internal fun InsightsRoute() {
    NothingFoundPlaceholder(onRetry = {}, modifier = Modifier.fillMaxSize())
}
