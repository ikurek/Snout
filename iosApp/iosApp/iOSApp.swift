import ComposeApp
import SwiftUI
import UIKit

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startSnout()
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
