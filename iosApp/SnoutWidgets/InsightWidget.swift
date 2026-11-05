import AppIntents
import SnoutWidgetKit
import SwiftUI
import WidgetKit

/// One saved insight reduced to what fits: its headline number (with its line when it is drawn as one) or its
/// largest breakdown values. The person picks the insight in the widget's settings.
struct InsightWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "InsightWidget", intent: SelectInsightIntent.self, provider: InsightProvider()) {
            InsightWidgetView(entry: $0)
        }
        .configurationDisplayName(Strings.insightName)
        .description(Strings.insightDescription)
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular])
        // So the chart can run to the edges; everything else keeps the margins itself (`margined`).
        .contentMarginsDisabled()
    }
}

// MARK: - Configuration

struct SelectInsightIntent: WidgetConfigurationIntent {
    static let title: LocalizedStringResource = "widget_insight_intent_title"
    static let description = IntentDescription("widget_insight_intent_description")

    @Parameter(title: "widget_parameter_project")
    var project: ProjectEntity?

    @Parameter(title: "widget_insight_name")
    var insight: InsightEntity?

    /// Project, then insight: the order Android's settings ask in. Its key is the English text (`Localizable.xcstrings`).
    static var parameterSummary: some ParameterSummary {
        Summary("From \(\.$project), show \(\.$insight)")
    }
}

/// An insight of a project: its id is "projectId:insightId", so a widget can tell an insight picked before its
/// project changed.
struct InsightEntity: AppEntity {
    static let typeDisplayRepresentation: TypeDisplayRepresentation = "widget_insight_name"
    static let defaultQuery = InsightQuery()

    let id: String
    let name: String

    init(projectId: Int64, insightId: Int64, name: String) {
        id = "\(projectId):\(insightId)"
        self.name = name
    }

    init(id: String, name: String) {
        self.id = id
        self.name = name
    }

    var projectId: Int64? { id.split(separator: ":").first.flatMap { Int64($0) } }
    var insightId: Int64? { id.split(separator: ":").last.flatMap { Int64($0) } }

    var displayRepresentation: DisplayRepresentation { DisplayRepresentation(title: "\(name)") }
}

/// The picked project's insights a widget can show, from PostHog each time the picker opens: nothing is kept between
/// refreshes. None until a project is picked.
struct InsightQuery: EntityStringQuery {
    @IntentParameterDependency<SelectInsightIntent>(\.$project)
    var intent

    func entities(for identifiers: [String]) async throws -> [InsightEntity] {
        let known = await choices("")
        return identifiers.map { id in known.first { $0.id == id } ?? InsightEntity(id: id, name: Strings.insightName) }
    }

    func entities(matching string: String) async throws -> [InsightEntity] {
        await choices(string)
    }

    func suggestedEntities() async throws -> [InsightEntity] {
        await choices("")
    }

    private func choices(_ query: String) async -> [InsightEntity] {
        // The dependency is nil until a project is picked.
        guard let project = intent?.project else { return [] }
        let projectId = Int64(project.id)
        SnoutWidgets.shared.start()
        guard let state = try? await SnoutWidgets.shared.insightChoices(projectId: projectId, query: query),
              case let .loaded(choices) = WidgetDisplay(state, content: { (list: NSArray) in
                  (list as? [InsightChoice] ?? []).map {
                      InsightEntity(projectId: projectId, insightId: $0.id, name: $0.name)
                  }
              })
        else { return [] }
        return choices
    }
}

// MARK: - Timeline

struct InsightEntry: TimelineEntry {
    let date: Date
    /// Nil until an insight is picked.
    let display: WidgetDisplay<InsightDisplay>?
    let name: String?
    /// The project's name, which heads the widget under the insight's.
    var project: String?
}

struct InsightProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> InsightEntry {
        InsightEntry(date: .now, display: .loaded(.placeholder), name: nil, project: Strings.previewProject)
    }

    func snapshot(for configuration: SelectInsightIntent, in context: Context) async -> InsightEntry {
        context.isPreview ? placeholder(in: context) : await load(configuration)
    }

    func timeline(for configuration: SelectInsightIntent, in context: Context) async -> Timeline<InsightEntry> {
        Timeline(entries: [await load(configuration)], policy: .after(.now.addingTimeInterval(RefreshInterval)))
    }

    private func load(_ configuration: SelectInsightIntent) async -> InsightEntry {
        guard let project = configuration.project else {
            return InsightEntry(date: .now, display: .noProject, name: nil)
        }
        // An insight picked under another project (the project changed after) is no pick at all.
        guard let insight = configuration.insight, insight.projectId == Int64(project.id),
              let insightId = insight.insightId
        else { return InsightEntry(date: .now, display: nil, name: nil, project: project.name) }
        let projectId = Int64(project.id)
        let display: WidgetDisplay<InsightDisplay>
        if let state = try? await SnoutWidgets.shared.insight(projectId: projectId, insightId: insightId) {
            display = WidgetDisplay(state) { (content: InsightWidgetContent) in InsightDisplay(content) }
        } else {
            display = .failed(Strings.errorUnexpected)
        }
        return InsightEntry(date: .now, display: display, name: insight.name, project: project.name)
    }
}

// MARK: - Views

struct InsightWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: InsightEntry

    var body: some View {
        content
            .widgetURL(link)
            .containerBackground(Ember.background, for: .widget)
    }

    private var link: URL? {
        if case let .loaded(insight) = entry.display { insight.link } else { nil }
    }

    @ViewBuilder
    private var content: some View {
        let title = entry.name ?? Strings.insightName
        switch entry.display {
        case nil:
            WidgetMessage(
                title: title,
                caption: entry.project,
                message: Strings.insightNotChosen,
                hint: family == .accessoryRectangular ? nil : Strings.hintEdit,
                centered: family != .accessoryRectangular
            )
            .margined()
        case let .loaded(insight):
            if family == .accessoryRectangular {
                RectangularInsight(insight: insight, project: entry.project).margined()
            } else {
                InsightContent(
                    insight: insight,
                    project: entry.project,
                    date: entry.date,
                    medium: family == .systemMedium
                )
            }
        case let .some(display):
            WidgetMessage(
                title: title,
                caption: entry.project,
                message: display.message ?? "",
                hint: family == .accessoryRectangular ? nil : display.hint,
                centered: family != .accessoryRectangular
            )
            .margined()
        }
    }
}

/// As Android's insight widget: the number, its change and what it counts over its line edge to edge, or the
/// largest breakdown values; then when it loaded.
private struct InsightContent: View {
    let insight: InsightDisplay
    let project: String?
    let date: Date
    let medium: Bool

    var body: some View {
        switch insight.body {
        case let .headline(value, label, change, series) where series.count > 1:
            WidgetChartLayout(values: series, date: date) {
                VStack(alignment: .leading, spacing: 2) {
                    WidgetHeader(title: insight.name, caption: project)
                    headline(value: value, label: label, change: change)
                }
            }
        default:
            VStack(alignment: .leading, spacing: 2) {
                WidgetHeader(title: insight.name, caption: project)
                switch insight.body {
                case let .headline(value, label, change, _):
                    headline(value: value, label: label, change: change)
                case let .ranking(rows):
                    RankingList(rows: rows, limit: medium ? 4 : 3).padding(.top, 2)
                case .unsupported:
                    Text(Strings.insightUnsupported)
                        .font(.system(size: 13))
                        .foregroundStyle(Ember.onBackground)
                        .lineLimit(3)
                        .padding(.top, 4)
                }
                Spacer(minLength: 0)
                WidgetFooter(date: date)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .margined()
        }
    }

    private func headline(value: String, label: String?, change: ChangeDisplay?) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            WidgetValue(text: value)
            HStack(alignment: .firstTextBaseline, spacing: 6) {
                if let change { ChangeText(change: change) }
                if let label { WidgetTitle(text: label, size: 11) }
            }
        }
    }
}

private struct RectangularInsight: View {
    let insight: InsightDisplay
    let project: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(joined(insight.name, project)).font(.caption).lineLimit(1)
            switch insight.body {
            case let .headline(value, _, change, _):
                Text(value)
                    .font(.title2.weight(.bold))
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
                    .privacySensitive()
                if let change { Text(change.text).font(.caption).privacySensitive() }
            case let .ranking(rows):
                if let top = rows.first {
                    Text(top.value).font(.title2.weight(.bold)).lineLimit(1).privacySensitive()
                    Text(top.label).font(.caption).lineLimit(1)
                }
            case .unsupported:
                Text(Strings.insightUnsupportedShort).font(.caption)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

#Preview(as: .systemSmall) {
    InsightWidget()
} timeline: {
    InsightEntry(date: .now, display: .loaded(.placeholder), name: Strings.previewInsight, project: Strings.previewProject)
    InsightEntry(date: .now, display: .loaded(.rankingPlaceholder), name: Strings.previewRanking, project: Strings.previewProject)
    InsightEntry(date: .now, display: .noProject, name: nil)
    InsightEntry(date: .now, display: nil, name: nil)
}

#Preview(as: .systemMedium) {
    InsightWidget()
} timeline: {
    InsightEntry(date: .now, display: .loaded(.placeholder), name: Strings.previewInsight, project: Strings.previewProject)
    InsightEntry(date: .now, display: .loaded(.rankingPlaceholder), name: Strings.previewRanking, project: Strings.previewProject)
}
