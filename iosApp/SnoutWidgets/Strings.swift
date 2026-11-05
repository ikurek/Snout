import Foundation

/// The widgets' text, from `Localizable.xcstrings`, under keys named as Android's `widget_*` strings. App Intents
/// read their titles at build time, so the intents and entities name their keys in place; everything else is here.
enum Strings {
    static var analyticsName: String { String(localized: "widget_analytics_name") }
    static var analyticsDescription: String { String(localized: "widget_analytics_description") }
    static var insightName: String { String(localized: "widget_insight_name") }
    static var insightDescription: String { String(localized: "widget_insight_description") }

    static var signedOut: String { String(localized: "widget_signed_out") }
    static var noProject: String { String(localized: "widget_no_project") }
    static var insightNotChosen: String { String(localized: "widget_insight_not_chosen") }
    static var insightUnsupported: String { String(localized: "widget_insight_unsupported") }
    static var insightUnsupportedShort: String { String(localized: "widget_insight_unsupported_short") }

    static var hintEdit: String { String(localized: "widget_hint_edit") }
    static var hintFailed: String { String(localized: "widget_hint_failed") }
    static var hintSignedOut: String { String(localized: "widget_hint_signed_out") }

    static var errorUnauthorized: String { String(localized: "widget_error_unauthorized") }
    static var errorForbidden: String { String(localized: "widget_error_forbidden") }
    static var errorPaymentRequired: String { String(localized: "widget_error_payment_required") }
    static var errorNotFound: String { String(localized: "widget_error_not_found") }
    static var errorRateLimited: String { String(localized: "widget_error_rate_limited") }
    static var errorOffline: String { String(localized: "widget_error_offline") }
    static var errorUnexpected: String { String(localized: "widget_error_unexpected") }

    static var refresh: String { String(localized: "widget_refresh") }

    /// "Updated 22:37".
    static func updated(_ time: String) -> String { String(localized: "widget_updated \(time)") }

    /// A Lock Screen line saying why there is nothing to show: "Snout: You're offline."
    static func inlineMessage(_ message: String) -> String { String(localized: "widget_inline_message \(message)") }

    /// A project the settings can no longer find by its id.
    static func projectId(_ id: Int) -> String { String(localized: "widget_project_id \(id)") }

    static var rangeToday: String { String(localized: "widget_range_today") }
    static var range7d: String { String(localized: "widget_range_7d") }
    static var range30d: String { String(localized: "widget_range_30d") }
    static var range90d: String { String(localized: "widget_range_90d") }

    static var metricVisitors: String { String(localized: "widget_metric_visitors") }
    static var metricViews: String { String(localized: "widget_metric_views") }
    static var metricSessions: String { String(localized: "widget_metric_sessions") }
    static var metricSessionDuration: String { String(localized: "widget_metric_session_duration") }
    static var metricBounceRate: String { String(localized: "widget_metric_bounce_rate") }
    static var metricWau: String { String(localized: "widget_metric_wau") }
    static var metricMau: String { String(localized: "widget_metric_mau") }
    static var metricOther: String { String(localized: "widget_metric_other") }

    /// The sample widgets' names, for the widget gallery and previews.
    static var previewProject: String { String(localized: "widget_preview_project") }
    static var previewInsight: String { String(localized: "widget_preview_insight") }
    static var previewEvent: String { String(localized: "widget_preview_event") }
    static var previewRanking: String { String(localized: "widget_preview_ranking") }
    static var previewDirect: String { String(localized: "widget_preview_direct") }
}
