import Foundation
import SnoutWidgetKit

/// What a widget draws, in plain Swift: the Kotlin states (`WidgetState` in :widgets) read once, so the views and
/// their previews never touch Kotlin types. Nothing outlives an entry: a failed refresh shows its error.
enum WidgetDisplay<Content> {
    case loaded(Content)
    case signedOut
    /// The widget has no project picked yet; nothing is loaded until it has.
    case noProject
    case failed(String)
}

struct AnalyticsDisplay {
    let range: String
    /// The widget's stat first, then the others.
    let stats: [StatDisplay]
    /// The widget's stat over the range; empty for stats PostHog has no trend of.
    let series: [Double]
    let link: URL?

    static var placeholder: AnalyticsDisplay { AnalyticsDisplay(
        range: Strings.range7d,
        stats: [
            StatDisplay(label: Strings.metricVisitors, value: "12.4k", change: ChangeDisplay(text: "↑ 12.8%", tone: .good)),
            StatDisplay(label: Strings.metricViews, value: "48.2k", change: ChangeDisplay(text: "↓ 5.5%", tone: .bad)),
            StatDisplay(label: Strings.metricSessions, value: "15k", change: ChangeDisplay(text: "↑ 0.1%", tone: .good)),
            StatDisplay(label: Strings.metricBounceRate, value: "42%", change: ChangeDisplay(text: "↑ 7.7%", tone: .bad)),
        ],
        series: [1500, 1720, 1650, 1980, 1210, 1890, 2480],
        link: nil
    ) }
}

struct InsightDisplay {
    enum Body {
        /// One number, with the line it is the latest point of when there is one.
        case headline(value: String, label: String?, change: ChangeDisplay?, series: [Double])
        /// Breakdown bars, pies and tables: the largest totals first.
        case ranking([RankedDisplay])
        /// Results a widget can't summarise; the widget offers to open them in the app.
        case unsupported
    }

    let name: String
    let body: Body
    let link: URL?

    static var placeholder: InsightDisplay { InsightDisplay(
        name: Strings.previewInsight,
        body: .headline(
            value: "280",
            label: Strings.previewEvent,
            change: nil,
            series: [120, 180, 90, 240, 200, 310, 280]
        ),
        link: nil
    ) }

    static var rankingPlaceholder: InsightDisplay { InsightDisplay(
        name: Strings.previewRanking,
        body: .ranking([
            RankedDisplay(label: Strings.previewDirect, value: "404"),
            RankedDisplay(label: "google.com", value: "212"),
            RankedDisplay(label: "news.ycombinator.com", value: "96"),
            RankedDisplay(label: "github.com", value: "41"),
            RankedDisplay(label: "x.com", value: "18"),
        ]),
        link: nil
    ) }
}

struct StatDisplay {
    let label: String
    let value: String
    let change: ChangeDisplay?
}

struct RankedDisplay {
    let label: String
    let value: String
}

struct ChangeDisplay {
    enum Tone { case good, bad, neutral }

    let text: String
    let tone: Tone
}

// MARK: - From Kotlin

extension WidgetDisplay {
    /// Reads a Kotlin `WidgetState`, turning its content with [content] when it loaded.
    init<Loaded: AnyObject>(_ state: any WidgetState, content: (Loaded) -> Content) {
        switch state {
        case let loaded as WidgetStateLoaded<Loaded>:
            self = .loaded(content(loaded.content))
        case let failed as WidgetStateFailed:
            self = .failed(failed.error.message)
        case is WidgetStateSignedOut:
            self = .signedOut
        default:
            self = .failed(DataError.unexpectederror.message)
        }
    }
}

extension AnalyticsDisplay {
    init(_ content: AnalyticsWidgetContent) {
        self.init(
            range: content.range.label,
            stats: content.stats.map { stat in
                StatDisplay(label: stat.key.label, value: stat.value, change: stat.change.map(ChangeDisplay.init))
            },
            series: content.series.map(\.doubleValue),
            link: content.link.flatMap { SnoutLink.url(carrying: $0) }
        )
    }
}

extension InsightDisplay {
    init(_ content: InsightWidgetContent) {
        let body: Body = switch content.body {
        case let headline as InsightWidgetBodyHeadline:
            .headline(
                value: headline.value,
                label: headline.label,
                change: headline.change.map(ChangeDisplay.init),
                series: headline.series.map(\.doubleValue)
            )
        case let ranking as InsightWidgetBodyRanking:
            .ranking(ranking.rows.map { RankedDisplay(label: $0.label, value: $0.value) })
        default:
            .unsupported
        }
        self.init(name: content.name, body: body, link: content.link.flatMap { SnoutLink.url(carrying: $0) })
    }
}

extension ChangeDisplay {
    init(_ change: TrendChange) {
        let tone: Tone = switch change.tone {
        case .good: .good
        case .bad: .bad
        default: .neutral
        }
        self.init(text: change.text, tone: tone)
    }
}

extension AnalyticsRange {
    var label: String {
        switch self {
        case .today: Strings.rangeToday
        case .last7days: Strings.range7d
        case .last30days: Strings.range30d
        default: Strings.range90d
        }
    }
}

extension OverviewMetricKey {
    /// The Analytics tab's names for the stats.
    var label: String {
        switch self {
        case .visitors: Strings.metricVisitors
        case .views: Strings.metricViews
        case .sessions: Strings.metricSessions
        case .sessionduration: Strings.metricSessionDuration
        case .bouncerate: Strings.metricBounceRate
        case .weeklyactiveusers: Strings.metricWau
        case .monthlyactiveusers: Strings.metricMau
        default: Strings.metricOther
        }
    }
}

private extension DataError {
    var message: String {
        switch self {
        case .unauthorized: Strings.errorUnauthorized
        case .forbidden: Strings.errorForbidden
        case .paymentrequired: Strings.errorPaymentRequired
        case .notfound: Strings.errorNotFound
        case .ratelimited: Strings.errorRateLimited
        case .connectionerror: Strings.errorOffline
        default: Strings.errorUnexpected
        }
    }
}
