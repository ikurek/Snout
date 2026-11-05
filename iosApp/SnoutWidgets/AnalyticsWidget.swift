import AppIntents
import SnoutWidgetKit
import SwiftUI
import WidgetKit

/// A project's analytics, for the stat and range the person picks in the widget's settings: PostHog's web analytics,
/// which count a mobile app's screens as they count a website's pages, as the app's Analytics tab does. That stat
/// when small or on the Lock Screen; when medium, laid out as Android's wide widget: the stat with its line beside it
/// and the next stats under it.
struct AnalyticsWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "AnalyticsWidget", intent: SelectAnalyticsIntent.self, provider: AnalyticsProvider()) {
            AnalyticsWidgetView(entry: $0)
        }
        .configurationDisplayName(Strings.analyticsName)
        .description(Strings.analyticsDescription)
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular, .accessoryInline])
        // So the chart can run to the edges; everything else keeps the margins itself (`margined`).
        .contentMarginsDisabled()
    }
}

// MARK: - Configuration

struct SelectAnalyticsIntent: WidgetConfigurationIntent {
    static let title: LocalizedStringResource = "widget_analytics_name"
    static let description = IntentDescription("widget_analytics_intent_description")

    @Parameter(title: "widget_parameter_project")
    var project: ProjectEntity?

    @Parameter(title: "widget_parameter_stat", default: .visitors)
    var stat: AnalyticsStatOption

    @Parameter(title: "widget_parameter_range", default: .last7Days)
    var range: AnalyticsRangeOption

    /// Project, stat, range: the order Android's settings ask in. Its key is the English text (`Localizable.xcstrings`).
    static var parameterSummary: some ParameterSummary {
        Summary("In \(\.$project), show \(\.$stat) for \(\.$range)")
    }
}

/// The stats a widget can lead with: `AnalyticsWidgetConfig.Metrics` in :widgets.
enum AnalyticsStatOption: String, AppEnum {
    case visitors, views, sessions, sessionDuration, bounceRate

    static let typeDisplayRepresentation: TypeDisplayRepresentation = "widget_parameter_stat"
    static let caseDisplayRepresentations: [AnalyticsStatOption: DisplayRepresentation] = [
        .visitors: "widget_metric_visitors",
        .views: "widget_metric_views",
        .sessions: "widget_metric_sessions",
        .sessionDuration: "widget_metric_session_duration",
        .bounceRate: "widget_metric_bounce_rate",
    ]

    var metric: OverviewMetricKey {
        switch self {
        case .visitors: .visitors
        case .views: .views
        case .sessions: .sessions
        case .sessionDuration: .sessionduration
        case .bounceRate: .bouncerate
        }
    }
}

enum AnalyticsRangeOption: String, AppEnum {
    case today, last7Days, last30Days, last90Days

    static let typeDisplayRepresentation: TypeDisplayRepresentation = "widget_parameter_range"
    static let caseDisplayRepresentations: [AnalyticsRangeOption: DisplayRepresentation] = [
        .today: "widget_range_today",
        .last7Days: "widget_range_7d",
        .last30Days: "widget_range_30d",
        .last90Days: "widget_range_90d",
    ]

    var range: AnalyticsRange {
        switch self {
        case .today: .today
        case .last7Days: .last7days
        case .last30Days: .last30days
        case .last90Days: .last90days
        }
    }
}

// MARK: - Timeline

struct AnalyticsEntry: TimelineEntry {
    let date: Date
    let display: WidgetDisplay<AnalyticsDisplay>
    /// What the widget is set up to show, which heads it whatever it loaded: the stat, and the project and range.
    var title = Strings.analyticsName
    var caption: String?
}

/// One entry per refresh, every 30 minutes as far as WidgetKit's budget allows, and whenever the app reloads its
/// widgets (signing in or out).
struct AnalyticsProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> AnalyticsEntry {
        AnalyticsEntry(date: .now, display: .loaded(.placeholder), title: Strings.metricVisitors, caption: joined(Strings.previewProject, Strings.range7d))
    }

    func snapshot(for configuration: SelectAnalyticsIntent, in context: Context) async -> AnalyticsEntry {
        context.isPreview ? placeholder(in: context) : await load(configuration)
    }

    func timeline(for configuration: SelectAnalyticsIntent, in context: Context) async -> Timeline<AnalyticsEntry> {
        Timeline(entries: [await load(configuration)], policy: .after(.now.addingTimeInterval(RefreshInterval)))
    }

    private func load(_ configuration: SelectAnalyticsIntent) async -> AnalyticsEntry {
        guard let project = configuration.project else { return AnalyticsEntry(date: .now, display: .noProject) }
        let title = configuration.stat.metric.label
        let caption = joined(project.name, configuration.range.range.label)
        let display: WidgetDisplay<AnalyticsDisplay>
        if let state = try? await SnoutWidgets.shared.analytics(
            projectId: Int64(project.id),
            metric: configuration.stat.metric,
            range: configuration.range.range
        ) {
            display = WidgetDisplay(state) { (content: AnalyticsWidgetContent) in AnalyticsDisplay(content) }
        } else {
            display = .failed(Strings.errorUnexpected)
        }
        return AnalyticsEntry(date: .now, display: display, title: title, caption: caption)
    }
}

/// How long an entry stands before WidgetKit asks for the next one.
let RefreshInterval: TimeInterval = 30 * 60

// MARK: - Views

struct AnalyticsWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: AnalyticsEntry

    var body: some View {
        content
            .widgetURL(link)
            .containerBackground(Ember.background, for: .widget)
    }

    private var link: URL? {
        if case let .loaded(analytics) = entry.display { analytics.link } else { nil }
    }

    @ViewBuilder
    private var content: some View {
        switch entry.display {
        case let .loaded(analytics):
            switch family {
            case .systemMedium: MediumAnalytics(analytics: analytics, entry: entry)
            case .accessoryInline: InlineAnalytics(analytics: analytics)
            case .accessoryRectangular: RectangularAnalytics(analytics: analytics, entry: entry).margined()
            default: SmallAnalytics(analytics: analytics, entry: entry)
            }
        default:
            if family == .accessoryInline {
                Text(Strings.inlineMessage(entry.display.message ?? ""))
            } else if family == .accessoryRectangular {
                WidgetMessage(title: entry.title, message: entry.display.message ?? "", centered: false).margined()
            } else {
                WidgetMessage(
                    title: entry.title,
                    caption: entry.caption,
                    message: entry.display.message ?? "",
                    hint: entry.display.hint
                )
                .margined()
            }
        }
    }
}

/// The stat and its change over its line, edge to edge: Android's widget two cells square.
private struct SmallAnalytics: View {
    let analytics: AnalyticsDisplay
    let entry: AnalyticsEntry

    var body: some View {
        if analytics.series.count > 1 {
            WidgetChartLayout(values: analytics.series, date: entry.date) { lead }
        } else {
            VStack(alignment: .leading, spacing: 2) {
                lead
                Spacer(minLength: 0)
                WidgetFooter(date: entry.date)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .margined()
        }
    }

    private var lead: some View {
        VStack(alignment: .leading, spacing: 2) {
            WidgetHeader(title: entry.title, caption: entry.caption)
            if let stat = analytics.stats.first {
                WidgetValue(text: stat.value)
                if let change = stat.change { ChangeText(change: change) }
            }
        }
    }
}

/// Android's wide widget: the stat and its change with the next stats beside them, over its line edge to edge.
/// Without a line, the next stats go under the stat, dropping their changes, then themselves, when they don't fit.
private struct MediumAnalytics: View {
    let analytics: AnalyticsDisplay
    let entry: AnalyticsEntry

    var body: some View {
        if analytics.series.count > 1 {
            WidgetChartLayout(values: analytics.series, date: entry.date) {
                VStack(alignment: .leading, spacing: 2) {
                    WidgetHeader(title: entry.title, caption: entry.caption)
                    HStack(alignment: .bottom, spacing: 12) {
                        lead.fixedSize()
                        ForEach(Array(analytics.stats.dropFirst().prefix(2).enumerated()), id: \.offset) { _, stat in
                            SmallStat(stat: stat, showsChange: true)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                }
            }
        } else {
            VStack(alignment: .leading, spacing: 0) {
                WidgetHeader(title: entry.title, caption: entry.caption)
                Spacer(minLength: 4)
                ViewThatFits(in: .vertical) {
                    layout(changes: true, footer: true)
                    layout(changes: true, footer: false)
                    layout(changes: false, footer: false)
                    lead
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .margined()
        }
    }

    private var lead: some View {
        VStack(alignment: .leading, spacing: 2) {
            if let stat = analytics.stats.first {
                WidgetValue(text: stat.value)
                if let change = stat.change { ChangeText(change: change) }
            }
        }
    }

    private func layout(changes: Bool, footer: Bool) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            lead
            HStack(alignment: .top, spacing: 8) {
                ForEach(Array(analytics.stats.dropFirst().prefix(3).enumerated()), id: \.offset) { _, stat in
                    SmallStat(stat: stat, showsChange: changes)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            if footer { WidgetFooter(date: entry.date).padding(.top, -10) }
        }
    }
}

/// Another stat of a wide widget, as Android draws it: its name over its value and change.
private struct SmallStat: View {
    let stat: StatDisplay
    let showsChange: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            WidgetTitle(text: stat.label, size: 11)
            WidgetValue(text: stat.value, large: false)
            if showsChange, let change = stat.change { ChangeText(change: change, size: 11) }
        }
    }
}

/// The Lock Screen's rectangle: the stat and its change, and the project, in the system's tint.
private struct RectangularAnalytics: View {
    let analytics: AnalyticsDisplay
    let entry: AnalyticsEntry

    var body: some View {
        if let stat = analytics.stats.first {
            VStack(alignment: .leading, spacing: 0) {
                Text(entry.caption ?? joined(stat.label, analytics.range)).font(.caption).lineLimit(1)
                Text(stat.value)
                    .font(.title2.weight(.bold))
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
                    .privacySensitive()
                Text(joined(stat.label, stat.change?.text)).font(.caption).lineLimit(1).privacySensitive()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct InlineAnalytics: View {
    let analytics: AnalyticsDisplay

    var body: some View {
        if let stat = analytics.stats.first {
            Text([stat.label, stat.value, stat.change?.text].compactMap { $0 }.joined(separator: " ")).privacySensitive()
        }
    }
}

#Preview(as: .systemSmall) {
    AnalyticsWidget()
} timeline: {
    AnalyticsEntry(date: .now, display: .loaded(.placeholder), title: Strings.metricVisitors, caption: joined(Strings.previewProject, Strings.range7d))
    AnalyticsEntry(date: .now, display: .failed(Strings.errorOffline), title: Strings.metricVisitors, caption: joined(Strings.previewProject, Strings.range7d))
    AnalyticsEntry(date: .now, display: .noProject)
}

#Preview(as: .systemMedium) {
    AnalyticsWidget()
} timeline: {
    AnalyticsEntry(date: .now, display: .loaded(.placeholder), title: Strings.metricVisitors, caption: joined(Strings.previewProject, Strings.range7d))
    AnalyticsEntry(
        date: .now,
        display: .loaded(AnalyticsDisplay(
            range: AnalyticsDisplay.placeholder.range,
            stats: AnalyticsDisplay.placeholder.stats.reversed(),
            series: [],
            link: nil
        )),
        title: Strings.metricBounceRate,
        caption: joined(Strings.previewProject, Strings.range7d)
    )
}
