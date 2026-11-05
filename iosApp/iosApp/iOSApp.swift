import ComposeApp
import SwiftUI
import UIKit
import WidgetKit

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startSnout()
        // Signing in or out: widgets load again rather than show the previous account.
        SnoutKt.observeWidgetChanges { WidgetCenter.shared.reloadAllTimelines() }
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                .onOpenURL { SnoutKt.submitIncomingLink(text: SnoutLink.text(from: $0)) }
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
