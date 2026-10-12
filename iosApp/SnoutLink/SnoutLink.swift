import Foundation

/// How the share extension hands shared text to the app: it opens snout://open?url=<text>, which the app reads back.
/// Compiled into both the app and the extension, so the two sides can't drift apart.
enum SnoutLink {
    private static let scheme = "snout"
    private static let host = "open"
    private static let textKey = "url"

    /// The URL that carries [text] (a link, or shared text containing one) into the app.
    static func url(carrying text: String) -> URL? {
        var components = URLComponents()
        components.scheme = scheme
        components.host = host
        components.queryItems = [URLQueryItem(name: textKey, value: text)]
        return components.url
    }

    /// The text a snout:// URL carries; any other URL the app is asked to open is the link itself.
    static func text(from url: URL) -> String {
        guard url.scheme == scheme else { return url.absoluteString }
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        return components?.queryItems?.first { $0.name == textKey }?.value ?? url.absoluteString
    }
}
