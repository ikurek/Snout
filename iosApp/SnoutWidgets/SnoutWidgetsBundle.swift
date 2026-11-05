import SnoutWidgetKit
import SwiftUI
import WidgetKit

/// Snout's home-screen and Lock Screen widgets. The extension runs Kotlin's widget graph (`SnoutWidgets` in
/// :widgets), reading the connection the app shares and loading what each widget shows on every refresh.
@main
struct SnoutWidgetsBundle: WidgetBundle {
    init() {
        SnoutWidgets.shared.start()
    }

    var body: some Widget {
        AnalyticsWidget()
        InsightWidget()
        HighlightsWidget()
    }
}
