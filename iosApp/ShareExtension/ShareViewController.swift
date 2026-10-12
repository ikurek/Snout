import UIKit
import UniformTypeIdentifiers

/// Share › Snout: iOS won't let Snout claim posthog.com links, so the share sheet is how a link gets in.
/// Hands the shared link (or text containing one) to the app as snout://open?url=… and closes, with no UI.
final class ShareViewController: UIViewController {
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        Task { await openInSnout() }
    }

    private func openInSnout() async {
        if let shared = await sharedLink(), let url = snoutURL(for: shared) {
            open(url)
        }
        extensionContext?.completeRequest(returningItems: nil)
    }

    /// The first shared web URL, else the first shared text; the app picks the PostHog link out of text.
    private func sharedLink() async -> String? {
        let items = extensionContext?.inputItems.compactMap { $0 as? NSExtensionItem } ?? []
        let providers = items.flatMap { $0.attachments ?? [] }
        for provider in providers where provider.hasItemConformingToTypeIdentifier(UTType.url.identifier) {
            if let url = try? await provider.loadItem(forTypeIdentifier: UTType.url.identifier) as? URL {
                return url.absoluteString
            }
        }
        for provider in providers where provider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) {
            if let text = try? await provider.loadItem(forTypeIdentifier: UTType.plainText.identifier) as? String {
                return text
            }
        }
        return nil
    }

    private func snoutURL(for shared: String) -> URL? {
        var components = URLComponents()
        components.scheme = "snout"
        components.host = "open"
        components.queryItems = [URLQueryItem(name: "url", value: shared)]
        return components.url
    }

    /// Extensions can't reach UIApplication.shared; the app object is still at the end of the responder chain.
    private func open(_ url: URL) {
        var responder: UIResponder? = self
        while let current = responder {
            if let application = current as? UIApplication {
                application.open(url, options: [:], completionHandler: nil)
                return
            }
            responder = current.next
        }
    }
}
