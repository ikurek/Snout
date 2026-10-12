package com.nightcode.snout.features.onboarding.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nightcode.snout.designsystem.atoms.BrandMark
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.app_name
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.ic_bug_report_24dp
import snout.composeapp.generated.resources.ic_flag_24dp
import snout.composeapp.generated.resources.ic_lock_outlined_24dp
import snout.composeapp.generated.resources.intro_screen_description
import snout.composeapp.generated.resources.intro_screen_feature_analytics_text
import snout.composeapp.generated.resources.intro_screen_feature_analytics_title
import snout.composeapp.generated.resources.intro_screen_feature_errors_text
import snout.composeapp.generated.resources.intro_screen_feature_errors_title
import snout.composeapp.generated.resources.intro_screen_feature_flags_text
import snout.composeapp.generated.resources.intro_screen_feature_flags_title
import snout.composeapp.generated.resources.intro_screen_feature_privacy_text
import snout.composeapp.generated.resources.intro_screen_feature_privacy_title
import snout.composeapp.generated.resources.intro_screen_header
import snout.composeapp.generated.resources.intro_screen_tagline

private class Feature(val icon: DrawableResource, val title: StringResource, val text: StringResource)

private val Features = listOf(
    Feature(
        Res.drawable.ic_analytics_24dp,
        Res.string.intro_screen_feature_analytics_title,
        Res.string.intro_screen_feature_analytics_text,
    ),
    Feature(
        Res.drawable.ic_flag_24dp,
        Res.string.intro_screen_feature_flags_title,
        Res.string.intro_screen_feature_flags_text,
    ),
    Feature(
        Res.drawable.ic_bug_report_24dp,
        Res.string.intro_screen_feature_errors_title,
        Res.string.intro_screen_feature_errors_text,
    ),
    Feature(
        Res.drawable.ic_lock_outlined_24dp,
        Res.string.intro_screen_feature_privacy_title,
        Res.string.intro_screen_feature_privacy_text,
    ),
)

private val BrandMarkSize = 72.dp
private val FeatureIconBoxSize = 40.dp
private val FeatureIconSize = 20.dp
private val EntranceOffset = 16.dp

/** Brand, header and description, then one item per feature. */
private val EntranceItems = 3 + Features.size
private const val EntranceMillis = 1_100
private const val EntranceItemShare = 0.45f

/** Brand, a one-line pitch and the four things Snout does, fading in once; [bottomPadding] clears the button. */
@Composable
internal fun IntroPage(bottomPadding: PaddingValues) {
    Content(contentPadding = bottomPadding, entrance = rememberEntrance())
}

@Composable
private fun Content(contentPadding: PaddingValues, entrance: Animatable<Float, AnimationVector1D>) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        VerticalSpacer(16.dp)
        Row(
            modifier = Modifier.entrance(entrance, index = 0),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandMark(size = BrandMarkSize)
            Column {
                Text(text = stringResource(Res.string.app_name), style = SnoutTheme.typography.headlineMedium)
                Text(
                    text = stringResource(Res.string.intro_screen_tagline),
                    style = SnoutTheme.typography.bodyMedium,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            }
        }
        VerticalSpacer(32.dp)
        Text(
            text = stringResource(Res.string.intro_screen_header),
            style = SnoutTheme.typography.headlineSmall,
            modifier = Modifier.entrance(entrance, index = 1).semantics { heading() },
        )
        VerticalSpacer(8.dp)
        Text(
            text = stringResource(Res.string.intro_screen_description),
            style = SnoutTheme.typography.bodyLarge,
            color = SnoutTheme.colors.onSurfaceVariant,
            modifier = Modifier.entrance(entrance, index = 2),
        )
        VerticalSpacer(32.dp)
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Features.forEachIndexed { index, feature ->
                FeatureRow(feature = feature, modifier = Modifier.entrance(entrance, index = 3 + index))
            }
        }
        VerticalSpacer(24.dp)
    }
}

/** Tonal icon tile beside a bold one-line title and a short description. */
@Composable
private fun FeatureRow(feature: Feature, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = SnoutTheme.shapes.medium,
            color = SnoutTheme.colors.secondaryContainer,
            contentColor = SnoutTheme.colors.onSecondaryContainer,
        ) {
            Box(modifier = Modifier.size(FeatureIconBoxSize), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(feature.icon),
                    contentDescription = null,
                    modifier = Modifier.size(FeatureIconSize),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(feature.title), style = SnoutTheme.typography.titleSmall)
            Text(
                text = stringResource(feature.text),
                style = SnoutTheme.typography.bodyMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
            )
        }
    }
}

/**
 * One timeline for the whole screen, played once: coming back from the login step shows everything in place.
 * Previews and screenshots start at the end.
 */
@Composable
private fun rememberEntrance(): Animatable<Float, AnimationVector1D> {
    var played by rememberSaveable { mutableStateOf(false) }
    val inspectionMode = LocalInspectionMode.current
    val entrance = remember { Animatable(if (played || inspectionMode) 1f else 0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(EntranceMillis, easing = LinearEasing))
        played = true
    }
    return entrance
}

/** Fades the [index]th item in and lifts it into place, each starting a little after the one above. */
private fun Modifier.entrance(entrance: Animatable<Float, AnimationVector1D>, index: Int): Modifier = graphicsLayer {
    val start = index * (1f - EntranceItemShare) / (EntranceItems - 1)
    val progress = FastOutSlowInEasing.transform(((entrance.value - start) / EntranceItemShare).coerceIn(0f, 1f))
    alpha = progress
    translationY = (1f - progress) * EntranceOffset.toPx()
}
