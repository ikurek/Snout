import AppIntents
import SwiftUI
import WidgetKit

/// The "Ember" colours of `composeApp/.../designsystem/tokens` the widgets use; keep them in step with the tokens.
enum Ember {
    static let background = dynamic(light: 0xFBF7F1, dark: 0x15120E)
    static let onBackground = dynamic(light: 0x1F1A14, dark: 0xF1EAE0)
    static let onSurfaceVariant = dynamic(light: 0x5E554B, dark: 0xB4A99A)
    /// The first chart series, as the app draws its lines.
    static let chart = dynamic(light: 0xBC3D0A, dark: 0xFF9466)
    static let good = dynamic(light: 0x276B43, dark: 0x7FD79F)
    static let bad = dynamic(light: 0xB3261E, dark: 0xFFB4AB)
    static let divider = dynamic(light: 0xE8E0D4, dark: 0x2E2822)
    /// A card on the background, such as a highlight's tile: the tokens' `surfaceContainer`.
    static let surfaceContainer = dynamic(light: 0xEDE5DA, dark: 0x231E18)

    private static func dynamic(light: UInt32, dark: UInt32) -> Color {
        Color(UIColor { $0.userInterfaceStyle == .dark ? UIColor(rgb: dark) : UIColor(rgb: light) })
    }
}

private extension UIColor {
    convenience init(rgb: UInt32) {
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255,
            green: CGFloat((rgb >> 8) & 0xFF) / 255,
            blue: CGFloat(rgb & 0xFF) / 255,
            alpha: 1
        )
    }
}

/// A small grey line, as Android's widgets draw a header's caption ([size] 12) and a stat's name (11).
struct WidgetTitle: View {
    let text: String
    var size: CGFloat = 12

    var body: some View {
        Text(text)
            .font(.system(size: size))
            .foregroundStyle(Ember.onSurfaceVariant)
            .lineLimit(1)
            .truncationMode(.tail)
    }
}

/// The widget's title over a [caption] (the project it shows, and what else tells it apart), with the refresh button
/// on the right: as Android's widgets head theirs. Lock Screen widgets, which can't be tapped apart, have no button.
struct WidgetHeader: View {
    @Environment(\.widgetFamily) private var family
    let title: String
    var caption: String?

    var body: some View {
        HStack(alignment: .top, spacing: 6) {
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(Ember.onBackground)
                    .lineLimit(1)
                if let caption { WidgetTitle(text: caption) }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            if family == .systemSmall || family == .systemMedium || family == .systemLarge {
                Button(intent: RefreshWidgetIntent()) {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Ember.onSurfaceVariant)
                        .frame(width: 22, height: 22)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Strings.refresh)
            }
        }
    }
}

/// Loads the widget again: WidgetKit reloads the timeline of the widget whose button ran it.
struct RefreshWidgetIntent: AppIntent {
    static let title: LocalizedStringResource = "widget_refresh"
    static let isDiscoverable = false

    func perform() async throws -> some IntentResult {
        .result()
    }
}

/// When the widget last loaded, at its foot, as Android's widgets say it: "Updated 22:37".
struct WidgetFooter: View {
    let date: Date

    var body: some View {
        WidgetTitle(text: Strings.updated(date.formatted(date: .omitted, time: .shortened)), size: 11)
            .padding(.top, 4)
    }
}

/// [parts] joined as the widgets join a project and a range: "Website · Last 7 days".
func joined(_ parts: String?...) -> String {
    parts.compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · ")
}

/// A number from the person's PostHog: hidden on a locked device when they don't allow widgets there.
/// In Android's sizes: 32 for the widget's number, 16 for the other stats of a wide one.
struct WidgetValue: View {
    let text: String
    var large = true

    var body: some View {
        Text(text)
            .font(.system(size: large ? 32 : 16, weight: .bold))
            .foregroundStyle(Ember.onBackground)
            .lineLimit(1)
            .minimumScaleFactor(0.5)
            .contentTransition(.numericText())
            .privacySensitive()
    }
}

/// The change as the app's trend chips write it, coloured by whether it is good news.
struct ChangeText: View {
    let change: ChangeDisplay
    var size: CGFloat = 12

    var body: some View {
        Text(change.text)
            .font(.system(size: size, weight: .medium))
            .foregroundStyle(color)
            .lineLimit(1)
            .privacySensitive()
    }

    private var color: Color {
        switch change.tone {
        case .good: Ember.good
        case .bad: Ember.bad
        case .neutral: Ember.onSurfaceVariant
        }
    }
}

/// [values] as a chart, as Android draws it: a line from the left edge to [endInset] short of the right one (room for
/// the dot on its latest point), over a fill that runs to both edges and fades to nothing at the bottom, keeping
/// [bottomRoom] free under the line for the footer.
struct Sparkline: View {
    let values: [Double]
    var bottomRoom: CGFloat = 0
    var endInset: CGFloat = 0

    var body: some View {
        GeometryReader { geometry in
            if values.count > 1, let min = values.min(), let max = values.max() {
                let points = points(in: geometry.size, min: min, max: max)
                ZStack {
                    Path { path in
                        path.move(to: CGPoint(x: 0, y: geometry.size.height))
                        points.forEach { path.addLine(to: $0) }
                        // The fill runs on to the right edge, past the dot.
                        path.addLine(to: CGPoint(x: geometry.size.width, y: points[points.count - 1].y))
                        path.addLine(to: CGPoint(x: geometry.size.width, y: geometry.size.height))
                        path.closeSubpath()
                    }
                    .fill(LinearGradient(
                        colors: [Ember.chart.opacity(0.3), Ember.chart.opacity(0)],
                        startPoint: .top,
                        endPoint: .bottom
                    ))
                    Path { path in
                        path.move(to: points[0])
                        points.dropFirst().forEach { path.addLine(to: $0) }
                    }
                    .stroke(Ember.chart, style: StrokeStyle(lineWidth: 2, lineCap: .round, lineJoin: .round))
                    Circle()
                        .fill(Ember.chart)
                        .frame(width: 7, height: 7)
                        .position(points[points.count - 1])
                }
            }
        }
        .privacySensitive()
    }

    /// From the left edge to [endInset] short of the right one, with room at the top so peaks aren't clipped.
    private func points(in size: CGSize, min: Double, max: Double) -> [CGPoint] {
        let span = max > min ? max - min : 1
        let top: CGFloat = 6
        let bottom = Swift.max(size.height - bottomRoom, top + 1)
        let stepX = (size.width - endInset) / CGFloat(values.count - 1)
        return values.enumerated().map { index, value in
            CGPoint(x: CGFloat(index) * stepX, y: top + (bottom - top) * CGFloat(1 - (value - min) / span))
        }
    }
}

/// [top] (the header and the number) inside the widget's margins, over [values] as a chart filling the rest of the
/// widget from its left edge and down to its bottom, ending on a dot a margin short of its right edge, with when it last loaded on the chart's faded fill: Android's layout.
/// The widget turns the system's margins off (`contentMarginsDisabled`) so the chart can reach the edges.
struct WidgetChartLayout<Top: View>: View {
    @Environment(\.widgetContentMargins) private var margins
    let values: [Double]
    let date: Date
    @ViewBuilder let top: Top

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            top.padding(EdgeInsets(top: margins.top, leading: margins.leading, bottom: 0, trailing: margins.trailing))
            ZStack(alignment: .bottomLeading) {
                Sparkline(values: values, bottomRoom: 34, endInset: 4)
                WidgetFooter(date: date)
                    .padding(.leading, margins.leading)
                    .padding(.bottom, 10)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

/// The widget's own margins, for content that doesn't run to the edges: the widgets turn the system's off.
struct Margined: ViewModifier {
    @Environment(\.widgetContentMargins) private var margins

    func body(content: Content) -> some View {
        content.padding(margins)
    }
}

extension View {
    func margined() -> some View { modifier(Margined()) }
}

/// Labelled values, largest first: as many rows as [limit] allows.
struct RankingList: View {
    let rows: [RankedDisplay]
    let limit: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            ForEach(Array(rows.prefix(limit).enumerated()), id: \.offset) { _, row in
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text(row.label)
                        .font(.system(size: 13))
                        .foregroundStyle(Ember.onBackground)
                        .lineLimit(1)
                        .truncationMode(.tail)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(row.value)
                        .font(.system(size: 13, weight: .bold))
                        .monospacedDigit()
                        .foregroundStyle(Ember.onBackground)
                        .lineLimit(1)
                        .fixedSize()
                        .privacySensitive()
                }
            }
        }
    }
}

/// Why there is nothing to show, under the widget's [title] and [caption]; [hint] says what to do about it.
/// Centred in the space under the header, unless not [centered] (the Lock Screen's small rectangle).
struct WidgetMessage: View {
    let title: String
    var caption: String?
    let message: String
    var hint: String?
    var centered = true

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            WidgetHeader(title: title, caption: caption)
            if centered {
                Spacer(minLength: 0)
                VStack(spacing: 4) {
                    messageText
                    hintText
                }
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                Spacer(minLength: 0)
            } else {
                messageText
                Spacer(minLength: 0)
                hintText
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }

    private var messageText: some View {
        Text(message)
            .font(.subheadline)
            .foregroundStyle(Ember.onBackground)
            .lineLimit(4)
            .minimumScaleFactor(0.8)
    }

    @ViewBuilder
    private var hintText: some View {
        if let hint {
            Text(hint)
                .font(.caption.weight(.medium))
                .foregroundStyle(Ember.chart)
                .lineLimit(2)
        }
    }
}

extension WidgetDisplay {
    /// The message for every state but [loaded], which the widget draws itself.
    var message: String? {
        switch self {
        case .loaded: nil
        case .signedOut: Strings.signedOut
        case .noProject: Strings.noProject
        case let .failed(message): message
        }
    }

    /// What a tap or the next refresh does about [message].
    var hint: String? {
        switch self {
        case .failed: Strings.hintFailed
        case .signedOut: Strings.hintSignedOut
        case .noProject: Strings.hintEdit
        case .loaded: nil
        }
    }
}
