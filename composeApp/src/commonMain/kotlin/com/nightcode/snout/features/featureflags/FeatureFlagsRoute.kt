package com.nightcode.snout.features.featureflags

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder

@Composable
internal fun FeatureFlagsRoute() {
    NothingFoundPlaceholder(onRetry = {}, modifier = Modifier.fillMaxSize())
}
