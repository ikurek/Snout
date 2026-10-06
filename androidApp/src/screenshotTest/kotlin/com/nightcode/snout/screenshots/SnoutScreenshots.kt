@file:Suppress("TooManyFunctions")

package com.nightcode.snout.screenshots

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.nightcode.snout.common.compose.ScreenshotEnvironment
import com.nightcode.snout.designsystem.atoms.AppIconPreview
import com.nightcode.snout.designsystem.atoms.AvatarBoxPreview
import com.nightcode.snout.designsystem.atoms.LabelChipPreview
import com.nightcode.snout.designsystem.atoms.LabelChipSuccessPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonDisabledLoadingPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonDisabledPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonDisabledWithIconPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonEnabledLoadingPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonEnabledPreview
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButtonEnabledWithIconPreview
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButtonDestructivePreview
import com.nightcode.snout.designsystem.atoms.buttons.SecondaryButtonPreview
import com.nightcode.snout.designsystem.atoms.chips.ActionChipPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldEmptyDisabledPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldEmptyPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldEmptyWithPlaceholderDisabledPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldEmptyWithPlaceholderPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldFilledDisabledPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldFilledErrorPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldFilledPreview
import com.nightcode.snout.designsystem.atoms.textfields.PrimaryTextFieldFilledWithIconsPreview
import com.nightcode.snout.designsystem.atoms.textfields.SearchFieldPreview
import com.nightcode.snout.designsystem.charts.BarChartPreview
import com.nightcode.snout.designsystem.charts.BoldNumberPreview
import com.nightcode.snout.designsystem.charts.DataTablePreview
import com.nightcode.snout.designsystem.charts.DonutChartPreview
import com.nightcode.snout.designsystem.charts.FunnelBarsPreview
import com.nightcode.snout.designsystem.charts.LineChartPreview
import com.nightcode.snout.designsystem.charts.RankedBarListPreview
import com.nightcode.snout.designsystem.molecules.EntityRowPreview
import com.nightcode.snout.designsystem.molecules.InfoNotePreview
import com.nightcode.snout.designsystem.molecules.InlineErrorPreview
import com.nightcode.snout.designsystem.molecules.KeyValueRowPreview
import com.nightcode.snout.designsystem.molecules.MetricCardPreview
import com.nightcode.snout.designsystem.molecules.SegmentedChoicePreview
import com.nightcode.snout.designsystem.molecules.TagListPreview
import com.nightcode.snout.designsystem.organisms.ConfirmationDialogPreview
import com.nightcode.snout.designsystem.organisms.GenericErrorPlaceholderPreview
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholderPreview
import com.nightcode.snout.designsystem.organisms.PaymentRequiredPlaceholderPreview
import com.nightcode.snout.designsystem.organisms.PermissionDeniedPlaceholderPreview
import com.nightcode.snout.designsystem.organisms.RateLimitedPlaceholderPreview
import com.nightcode.snout.features.activitylog.ActivityLogScreenPaymentRequiredPreview
import com.nightcode.snout.features.activitylog.ActivityLogScreenPreview
import com.nightcode.snout.features.analytics.AnalyticsScreenErrorPreview
import com.nightcode.snout.features.analytics.AnalyticsScreenLoadingPreview
import com.nightcode.snout.features.analytics.AnalyticsScreenPreview
import com.nightcode.snout.features.analytics.AnalyticsScreenRefreshErrorPreview
import com.nightcode.snout.features.annotations.AnnotationsScreenEmptyPreview
import com.nightcode.snout.features.annotations.AnnotationsScreenPreview
import com.nightcode.snout.features.dashboards.DashboardsScreenEmptyPreview
import com.nightcode.snout.features.dashboards.DashboardsScreenErrorPreview
import com.nightcode.snout.features.dashboards.DashboardsScreenLoadingPreview
import com.nightcode.snout.features.dashboards.DashboardsScreenPreview
import com.nightcode.snout.features.dashboards.detail.DashboardDetailScreenErrorPreview
import com.nightcode.snout.features.dashboards.detail.DashboardDetailScreenPreview
import com.nightcode.snout.features.dashboards.detail.DashboardDetailScreenTilesPendingPreview
import com.nightcode.snout.features.experiments.ExperimentsScreenEmptyPreview
import com.nightcode.snout.features.experiments.ExperimentsScreenPreview
import com.nightcode.snout.features.experiments.detail.ExperimentDetailScreenPreview
import com.nightcode.snout.features.featureflags.FeatureFlagsScreenEmptyPreview
import com.nightcode.snout.features.featureflags.FeatureFlagsScreenForbiddenPreview
import com.nightcode.snout.features.featureflags.FeatureFlagsScreenPreview
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailScreenForbiddenPreview
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailScreenPreview
import com.nightcode.snout.features.insights.InsightsScreenEmptyPreview
import com.nightcode.snout.features.insights.InsightsScreenPreview
import com.nightcode.snout.features.insights.detail.InsightDetailScreenPendingPreview
import com.nightcode.snout.features.insights.detail.InsightDetailScreenPreview
import com.nightcode.snout.features.intialproject.InitialProjectScreenErrorPreview
import com.nightcode.snout.features.intialproject.InitialProjectScreenLoadedPreview
import com.nightcode.snout.features.intialproject.InitialProjectScreenLoadingPreview
import com.nightcode.snout.features.login.LoginScreenConnectingPreview
import com.nightcode.snout.features.login.LoginScreenEmptyPreview
import com.nightcode.snout.features.login.LoginScreenErrorsPreview
import com.nightcode.snout.features.more.MoreScreenPreview
import com.nightcode.snout.features.settings.SettingsScreenAccountErrorPreview
import com.nightcode.snout.features.settings.SettingsScreenPreview
import com.nightcode.snout.features.splash.SplashScreenPreview

private const val Phone = "spec:width=411dp,height=891dp"

// Generated from the @Preview functions in composeApp; one light and one dark reference image per preview.

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppIconPreviewShot() = ScreenshotEnvironment { AppIconPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AvatarBoxPreviewShot() = ScreenshotEnvironment { AvatarBoxPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LabelChipPreviewShot() = ScreenshotEnvironment { LabelChipPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LabelChipSuccessPreviewShot() = ScreenshotEnvironment { LabelChipSuccessPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonDisabledLoadingPreviewShot() = ScreenshotEnvironment { PrimaryButtonDisabledLoadingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonDisabledPreviewShot() = ScreenshotEnvironment { PrimaryButtonDisabledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonDisabledWithIconPreviewShot() = ScreenshotEnvironment { PrimaryButtonDisabledWithIconPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonEnabledLoadingPreviewShot() = ScreenshotEnvironment { PrimaryButtonEnabledLoadingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonEnabledPreviewShot() = ScreenshotEnvironment { PrimaryButtonEnabledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryButtonEnabledWithIconPreviewShot() = ScreenshotEnvironment { PrimaryButtonEnabledWithIconPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SecondaryButtonDestructivePreviewShot() = ScreenshotEnvironment { SecondaryButtonDestructivePreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SecondaryButtonPreviewShot() = ScreenshotEnvironment { SecondaryButtonPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ActionChipPreviewShot() = ScreenshotEnvironment { ActionChipPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldEmptyDisabledPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldEmptyDisabledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldEmptyPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldEmptyWithPlaceholderDisabledPreviewShot() =
    ScreenshotEnvironment { PrimaryTextFieldEmptyWithPlaceholderDisabledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldEmptyWithPlaceholderPreviewShot() =
    ScreenshotEnvironment { PrimaryTextFieldEmptyWithPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldFilledDisabledPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldFilledDisabledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldFilledErrorPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldFilledErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldFilledPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldFilledPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PrimaryTextFieldFilledWithIconsPreviewShot() = ScreenshotEnvironment { PrimaryTextFieldFilledWithIconsPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchFieldPreviewShot() = ScreenshotEnvironment { SearchFieldPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BarChartPreviewShot() = ScreenshotEnvironment { BarChartPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BoldNumberPreviewShot() = ScreenshotEnvironment { BoldNumberPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DataTablePreviewShot() = ScreenshotEnvironment { DataTablePreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DonutChartPreviewShot() = ScreenshotEnvironment { DonutChartPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FunnelBarsPreviewShot() = ScreenshotEnvironment { FunnelBarsPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LineChartPreviewShot() = ScreenshotEnvironment { LineChartPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RankedBarListPreviewShot() = ScreenshotEnvironment { RankedBarListPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EntityRowPreviewShot() = ScreenshotEnvironment { EntityRowPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InfoNotePreviewShot() = ScreenshotEnvironment { InfoNotePreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InlineErrorPreviewShot() = ScreenshotEnvironment { InlineErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun KeyValueRowPreviewShot() = ScreenshotEnvironment { KeyValueRowPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MetricCardPreviewShot() = ScreenshotEnvironment { MetricCardPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SegmentedChoicePreviewShot() = ScreenshotEnvironment { SegmentedChoicePreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagListPreviewShot() = ScreenshotEnvironment { TagListPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfirmationDialogPreviewShot() = ScreenshotEnvironment { ConfirmationDialogPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericErrorPlaceholderPreviewShot() = ScreenshotEnvironment { GenericErrorPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NothingFoundPlaceholderPreviewShot() = ScreenshotEnvironment { NothingFoundPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PaymentRequiredPlaceholderPreviewShot() = ScreenshotEnvironment { PaymentRequiredPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PermissionDeniedPlaceholderPreviewShot() = ScreenshotEnvironment { PermissionDeniedPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RateLimitedPlaceholderPreviewShot() = ScreenshotEnvironment { RateLimitedPlaceholderPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ActivityLogScreenPaymentRequiredPreviewShot() = ScreenshotEnvironment { ActivityLogScreenPaymentRequiredPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ActivityLogScreenPreviewShot() = ScreenshotEnvironment { ActivityLogScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnalyticsScreenErrorPreviewShot() = ScreenshotEnvironment { AnalyticsScreenErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnalyticsScreenLoadingPreviewShot() = ScreenshotEnvironment { AnalyticsScreenLoadingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnalyticsScreenPreviewShot() = ScreenshotEnvironment { AnalyticsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnalyticsScreenRefreshErrorPreviewShot() = ScreenshotEnvironment { AnalyticsScreenRefreshErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnnotationsScreenEmptyPreviewShot() = ScreenshotEnvironment { AnnotationsScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AnnotationsScreenPreviewShot() = ScreenshotEnvironment { AnnotationsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardsScreenEmptyPreviewShot() = ScreenshotEnvironment { DashboardsScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardsScreenErrorPreviewShot() = ScreenshotEnvironment { DashboardsScreenErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardsScreenLoadingPreviewShot() = ScreenshotEnvironment { DashboardsScreenLoadingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardsScreenPreviewShot() = ScreenshotEnvironment { DashboardsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardDetailScreenErrorPreviewShot() = ScreenshotEnvironment { DashboardDetailScreenErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardDetailScreenPreviewShot() = ScreenshotEnvironment { DashboardDetailScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardDetailScreenTilesPendingPreviewShot() =
    ScreenshotEnvironment { DashboardDetailScreenTilesPendingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExperimentsScreenEmptyPreviewShot() = ScreenshotEnvironment { ExperimentsScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExperimentsScreenPreviewShot() = ScreenshotEnvironment { ExperimentsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExperimentDetailScreenPreviewShot() = ScreenshotEnvironment { ExperimentDetailScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FeatureFlagsScreenEmptyPreviewShot() = ScreenshotEnvironment { FeatureFlagsScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FeatureFlagsScreenForbiddenPreviewShot() = ScreenshotEnvironment { FeatureFlagsScreenForbiddenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FeatureFlagsScreenPreviewShot() = ScreenshotEnvironment { FeatureFlagsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FeatureFlagDetailScreenForbiddenPreviewShot() = ScreenshotEnvironment { FeatureFlagDetailScreenForbiddenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FeatureFlagDetailScreenPreviewShot() = ScreenshotEnvironment { FeatureFlagDetailScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InsightsScreenEmptyPreviewShot() = ScreenshotEnvironment { InsightsScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InsightsScreenPreviewShot() = ScreenshotEnvironment { InsightsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InsightDetailScreenPendingPreviewShot() = ScreenshotEnvironment { InsightDetailScreenPendingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InsightDetailScreenPreviewShot() = ScreenshotEnvironment { InsightDetailScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InitialProjectScreenErrorPreviewShot() = ScreenshotEnvironment { InitialProjectScreenErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InitialProjectScreenLoadedPreviewShot() = ScreenshotEnvironment { InitialProjectScreenLoadedPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InitialProjectScreenLoadingPreviewShot() = ScreenshotEnvironment { InitialProjectScreenLoadingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenConnectingPreviewShot() = ScreenshotEnvironment { LoginScreenConnectingPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenEmptyPreviewShot() = ScreenshotEnvironment { LoginScreenEmptyPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenErrorsPreviewShot() = ScreenshotEnvironment { LoginScreenErrorsPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MoreScreenPreviewShot() = ScreenshotEnvironment { MoreScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenAccountErrorPreviewShot() = ScreenshotEnvironment { SettingsScreenAccountErrorPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreviewShot() = ScreenshotEnvironment { SettingsScreenPreview() }

@PreviewTest
@Preview(name = "light", showBackground = true, device = Phone)
@Preview(name = "dark", showBackground = true, device = Phone, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SplashScreenPreviewShot() = ScreenshotEnvironment { SplashScreenPreview() }
