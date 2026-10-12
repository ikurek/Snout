import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    private let incomingLinks = IncomingLinks()

    var body: some Scene {
        WindowGroup {
            ContentView(incomingLinks: incomingLinks)
                .onOpenURL { incomingLinks.open(text: Self.linkText(from: $0)) }
        }
    }

    /// snout://open?url=… from the share extension carries the shared text; any other URL is the link itself.
    private static func linkText(from url: URL) -> String {
        guard url.scheme == "snout" else { return url.absoluteString }
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        return components?.queryItems?.first { $0.name == "url" }?.value ?? url.absoluteString
    }
}
