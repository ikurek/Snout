import AppIntents
import SnoutWidgetKit
import SwiftUI
import WidgetKit

/// A project's highlights, picked per widget from its web stats, active users, revenue and app health: a grid of tiles
/// as Android draws it, up to two by two when small, three by two when medium and three by three when large, each a
/// metric's value and its change over the widget's range. On the Lock Screen, the first metrics.
struct HighlightsWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "HighlightsWidget", intent: SelectHighlightsIntent.self, provider: HighlightsProvider()) {
            HighlightsWidgetView(entry: $0)
        }
        .configurationDisplayName(Strings.highlightsName)
        .description(Strings.highlightsDescription)
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge, .accessoryRectangular, .accessoryInline])
        .contentMarginsDisabled()
    }
}

// MARK: - Configuration

struct SelectHighlightsIntent: WidgetConfigurationIntent {
    static let title: LocalizedStringResource = "widget_highlights_name"
    static let description = IntentDescription("widget_highlights_intent_description")

    @Parameter(title: "widget_parameter_project")
    var project: ProjectEntity?

    @Parameter(title: "widget_parameter_range", default: .last7Days)
    var range: AnalyticsRangeOption

    /// As many as the widget shows: four when small, six when medium, nine when large (`HighlightsWidgetConfig.MaxMetrics`).
    @Parameter(
        title: "widget_parameter_metrics",
        default: HighlightMetricEntity.defaults,
        size: [.systemSmall: 4, .systemMedium: 6, .systemLarge: 9, .accessoryRectangular: 2, .accessoryInline: 1]
    )
    var metrics: [HighlightMetricEntity]

    /// Project, range, metrics: the order Android's settings ask in. Its key is the English text (`Localizable.xcstrings`).
    static var parameterSummary: some ParameterSummary {
        Summary("In \(\.$project), show \(\.$metrics) for \(\.$range)")
    }
}

/// A metric a widget can show: an entity rather than an `AppEnum`, since only entities take a count per widget size.
struct HighlightMetricEntity: AppEntity {
    static let typeDisplayRepresentation: TypeDisplayRepresentation = "widget_parameter_metric"
    static let defaultQuery = HighlightMetricQuery()

    /// Daily and monthly users, session duration and exceptions, as on Android (`HighlightsWidgetConfig.DefaultMetrics`).
    static var defaults: [HighlightMetricEntity] {
        [.dailyActiveUsers, .monthlyActiveUsers, .sessionDuration, .exceptions].map(HighlightMetricEntity.init)
    }

    let option: HighlightMetricOption

    init(_ option: HighlightMetricOption) { self.option = option }

    var id: String { option.rawValue }

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: LocalizedStringResource(String.LocalizationValue(option.titleKey)))
    }
}

/// Every metric, in the order Android's settings list them.
struct HighlightMetricQuery: EnumerableEntityQuery {
    func allEntities() async throws -> [HighlightMetricEntity] {
        HighlightMetricOption.allCases.map(HighlightMetricEntity.init)
    }

    func entities(for identifiers: [String]) async throws -> [HighlightMetricEntity] {
        identifiers.compactMap(HighlightMetricOption.init(rawValue:)).map(HighlightMetricEntity.init)
    }

    func suggestedEntities() async throws -> [HighlightMetricEntity] {
        try await allEntities()
    }
}

/// The metrics a widget can show, in the order Android's settings list them: `HighlightMetric` in :shared.
enum HighlightMetricOption: String, CaseIterable {
    case visitors, views, sessions, sessionDuration, bounceRate
    case dailyActiveUsers, weeklyActiveUsers, monthlyActiveUsers, stickiness
    case mrr, revenue, subscribers, payingCustomers
    case latestAppVersion, exceptions, usersWithErrors

    /// Its name's key in `Localizable.xcstrings`.
    var titleKey: String {
        switch self {
        case .visitors: "widget_metric_visitors"
        case .views: "widget_metric_views"
        case .sessions: "widget_metric_sessions"
        case .sessionDuration: "widget_metric_session_duration"
        case .bounceRate: "widget_metric_bounce_rate"
        case .dailyActiveUsers: "widget_metric_dau"
        case .weeklyActiveUsers: "widget_metric_wau"
        case .monthlyActiveUsers: "widget_metric_mau"
        case .stickiness: "widget_metric_stickiness"
        case .mrr: "widget_metric_mrr"
        case .revenue: "widget_metric_revenue"
        case .subscribers: "widget_metric_subscribers"
        case .payingCustomers: "widget_metric_paying_customers"
        case .latestAppVersion: "widget_metric_app_version"
        case .exceptions: "widget_metric_exceptions"
        case .usersWithErrors: "widget_metric_users_with_errors"
        }
    }

    var metric: HighlightMetric {
        switch self {
        case .visitors: .visitors
        case .views: .views
        case .sessions: .sessions
        case .sessionDuration: .sessionduration
        case .bounceRate: .bouncerate
        case .dailyActiveUsers: .dailyactiveusers
        case .weeklyActiveUsers: .weeklyactiveusers
        case .monthlyActiveUsers: .monthlyactiveusers
        case .stickiness: .stickiness
        case .mrr: .mrr
        case .revenue: .revenue
        case .subscribers: .subscribers
        case .payingCustomers: .payingcustomers
        case .latestAppVersion: .latestappversion
        case .exceptions: .exceptions
        case .usersWithErrors: .userswitherrors
        }
    }
}

// MARK: - Timeline

struct HighlightsEntry: TimelineEntry {
    let date: Date
    let display: WidgetDisplay<HighlightsDisplay>
    /// What the widget is set up to show, which heads it whatever it loaded: the project, and the range.
    var title = Strings.highlightsName
    var caption: String?
}

/// One entry per refresh, every 30 minutes as far as WidgetKit's budget allows, and whenever the app reloads its
/// widgets (signing in or out).
struct HighlightsProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> HighlightsEntry {
        HighlightsEntry(date: .now, display: .loaded(.placeholder), title: Strings.previewProject, caption: Strings.range7d)
    }

    func snapshot(for configuration: SelectHighlightsIntent, in context: Context) async -> HighlightsEntry {
        context.isPreview ? placeholder(in: context) : await load(configuration)
    }

    func timeline(for configuration: SelectHighlightsIntent, in context: Context) async -> Timeline<HighlightsEntry> {
        Timeline(entries: [await load(configuration)], policy: .after(.now.addingTimeInterval(RefreshInterval)))
    }

    private func load(_ configuration: SelectHighlightsIntent) async -> HighlightsEntry {
        guard let project = configuration.project else { return HighlightsEntry(date: .now, display: .noProject) }
        let caption = configuration.range.range.label
        let display: WidgetDisplay<HighlightsDisplay>
        if let state = try? await SnoutWidgets.shared.highlights(
            projectId: Int64(project.id),
            range: configuration.range.range,
            metrics: configuration.metrics.map(\.option.metric)
        ) {
            display = WidgetDisplay(state) { (content: HighlightsWidgetContent) in HighlightsDisplay(content) }
        } else {
            display = .failed(Strings.errorUnexpected)
        }
        return HighlightsEntry(date: .now, display: display, title: project.name, caption: caption)
    }
}

// MARK: - Views

struct HighlightsWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: HighlightsEntry

    var body: some View {
        content
            .widgetURL(link)
            .containerBackground(Ember.background, for: .widget)
    }

    private var link: URL? {
        if case let .loaded(highlights) = entry.display { highlights.link } else { nil }
    }

    @ViewBuilder
    private var content: some View {
        switch entry.display {
        case let .loaded(highlights):
            switch family {
            case .accessoryInline: InlineHighlights(highlights: highlights)
            case .accessoryRectangular: RectangularHighlights(highlights: highlights, entry: entry).margined()
            default: HighlightsGrid(highlights: highlights, entry: entry, shape: GridShape(family: family, count: highlights.tiles.count))
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

/// How a widget lays out [count] tiles: the fewest columns, so the widest tiles, its family has room for.
struct GridShape: Equatable {
    let columns: Int
    let rows: Int

    var shown: Int { columns * rows }

    init(columns: Int, rows: Int) {
        self.columns = columns
        self.rows = rows
    }

    init(family: WidgetFamily, count: Int) {
        let count = max(count, 1)
        switch family {
        case .systemSmall:
            self.init(columns: count == 1 ? 1 : 2, rows: count <= 2 ? 1 : 2)
        case .systemMedium:
            self.init(columns: count <= 4 ? min(count, 2) : 3, rows: count <= 2 ? 1 : 2)
        default:
            // Large: two by two, two by three, then three by three.
            let columns = count == 1 ? 1 : count <= 6 ? 2 : 3
            self.init(columns: columns, rows: min((count + columns - 1) / columns, 3))
        }
    }
}

/// The header, then the tiles in rows of [shape]'s columns, each row as tall as the others, and the footer when it
/// fits: as Android draws the widget. The header says how many tiles didn't fit.
private struct HighlightsGrid: View {
    @Environment(\.widgetFamily) private var family
    let highlights: HighlightsDisplay
    let entry: HighlightsEntry
    let shape: GridShape

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            WidgetHeader(title: entry.title, caption: caption)
            ViewThatFits(in: .vertical) {
                grid(changes: true, footer: true)
                grid(changes: true, footer: false)
                grid(changes: false, footer: false)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        // Less than the system's margins, as on Android: the tiles' cards carry their own, and two rows of them need
        // the height of a small widget.
        .padding(12)
    }

    /// As large as the tiles' height allows: a large widget's two rows read across the room, two rows of a small or
    /// medium one only fit at 20.
    private var valueSize: CGFloat {
        switch (shape.rows, family) {
        case (1, _): 28
        case (2, .systemLarge): 34
        case (_, .systemLarge): 24
        default: 20
        }
    }

    private var caption: String? {
        let hidden = highlights.tiles.count - min(highlights.tiles.count, shape.shown)
        guard hidden > 0, let caption = entry.caption else { return entry.caption }
        return Strings.rangeMore(caption, hidden)
    }

    private func grid(changes: Bool, footer: Bool) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                HStack(spacing: 6) {
                    ForEach(Array(row.enumerated()), id: \.offset) { _, tile in
                        TileView(tile: tile, showsChange: changes, valueSize: valueSize)
                    }
                    // A short last row lines up with the rows above.
                    ForEach(0 ..< (shape.columns - row.count), id: \.self) { _ in
                        Color.clear.frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                }
                .frame(maxHeight: .infinity)
            }
            if footer { WidgetFooter(date: entry.date).padding(.top, -4) }
        }
    }

    private var rows: [[TileDisplay]] {
        let shown = Array(highlights.tiles.prefix(shape.shown))
        let columns = shape.columns
        return stride(from: 0, to: shown.count, by: columns).map { Array(shown[$0 ..< min($0 + columns, shown.count)]) }
    }
}

/// A card with a metric's name over its value, then its change; without room for the change, an arrow by the name
/// says which way it went. A tile without a value says why in its place.
private struct TileView: View {
    let tile: TileDisplay
    let showsChange: Bool
    let valueSize: CGFloat

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // The arrow only when the name still fits beside it: the name says what the number is.
            ViewThatFits(in: .horizontal) {
                HStack(spacing: 2) {
                    Text(tile.label)
                        .font(.system(size: 11))
                        .foregroundStyle(Ember.onSurfaceVariant)
                        .lineLimit(1)
                        .fixedSize()
                    if !showsChange, let change = tile.change {
                        Spacer(minLength: 4)
                        ChangeText(change: ChangeDisplay(text: arrow(change.text), tone: change.tone), size: 11)
                    }
                }
                WidgetTitle(text: tile.label, size: 11)
            }
            if let value = tile.value {
                Text(value)
                    .font(.system(size: valueSize, weight: .bold))
                    .foregroundStyle(Ember.onBackground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                    .contentTransition(.numericText())
                    .privacySensitive()
            } else {
                Text(tile.note ?? "–")
                    .font(.system(size: 12))
                    .foregroundStyle(Ember.onSurfaceVariant)
                    .lineLimit(2)
                    .minimumScaleFactor(0.8)
            }
            if showsChange, let change = tile.change {
                ChangeText(change: change, size: 11)
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .background(Ember.surfaceContainer, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
    }

    /// Just the arrow of a change ("↑").
    private func arrow(_ text: String) -> String {
        text.split(separator: " ").first.map(String.init) ?? text
    }
}

/// The Lock Screen's rectangle: the project and range over the first two metrics side by side, in the system's tint.
private struct RectangularHighlights: View {
    let highlights: HighlightsDisplay
    let entry: HighlightsEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(joined(entry.title, entry.caption)).font(.caption).lineLimit(1)
            HStack(alignment: .top, spacing: 8) {
                ForEach(Array(highlights.tiles.prefix(2).enumerated()), id: \.offset) { _, tile in
                    VStack(alignment: .leading, spacing: 0) {
                        Text(tile.value ?? "–")
                            .font(.headline.weight(.bold))
                            .minimumScaleFactor(0.6)
                            .lineLimit(1)
                            .privacySensitive()
                        Text(tile.label).font(.caption2).lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct InlineHighlights: View {
    let highlights: HighlightsDisplay

    var body: some View {
        if let tile = highlights.tiles.first {
            Text([tile.label, tile.value ?? "–", tile.change?.text].compactMap { $0 }.joined(separator: " "))
                .privacySensitive()
        }
    }
}

#Preview(as: .systemSmall) {
    HighlightsWidget()
} timeline: {
    HighlightsEntry(date: .now, display: .loaded(.placeholder), title: Strings.previewProject, caption: Strings.range7d)
    HighlightsEntry(date: .now, display: .failed(Strings.errorOffline), title: Strings.previewProject, caption: Strings.range7d)
    HighlightsEntry(date: .now, display: .noProject)
}

#Preview(as: .systemMedium) {
    HighlightsWidget()
} timeline: {
    HighlightsEntry(date: .now, display: .loaded(.placeholder), title: Strings.previewProject, caption: Strings.range7d)
}

#Preview(as: .systemLarge) {
    HighlightsWidget()
} timeline: {
    HighlightsEntry(date: .now, display: .loaded(.placeholder), title: Strings.previewProject, caption: Strings.range7d)
}
