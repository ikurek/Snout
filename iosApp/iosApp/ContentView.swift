import ComposeApp
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    let incomingLinks: IncomingLinks

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(incomingLinks: incomingLinks)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let incomingLinks: IncomingLinks

    var body: some View {
        ComposeView(incomingLinks: incomingLinks)
            .ignoresSafeArea()
    }
}
