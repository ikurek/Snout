import AppIntents
import SnoutWidgetKit

/// A PostHog project a widget shows, picked in the widget's settings; named with its organization, since project
/// names repeat across organizations.
struct ProjectEntity: AppEntity {
    static let typeDisplayRepresentation: TypeDisplayRepresentation = "widget_parameter_project"
    static let defaultQuery = ProjectQuery()

    let id: Int
    let name: String
    let organization: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(name)", subtitle: "\(organization)")
    }
}

/// Every project the key can read, from PostHog each time the settings open. A new widget starts on none, as on
/// Android: the person picks its project, which needn't be the one the app shows.
struct ProjectQuery: EntityQuery {
    func entities(for identifiers: [Int]) async throws -> [ProjectEntity] {
        let known = await projects()
        return identifiers.map { id in
            known.first { $0.id == id } ?? ProjectEntity(id: id, name: Strings.projectId(id), organization: "")
        }
    }

    func suggestedEntities() async throws -> [ProjectEntity] {
        await projects()
    }

    private func projects() async -> [ProjectEntity] {
        SnoutWidgets.shared.start()
        guard let state = try? await SnoutWidgets.shared.projects(),
              case let .loaded(projects) = WidgetDisplay(state, content: { (list: NSArray) in
                  (list as? [ProjectChoice] ?? []).map {
                      ProjectEntity(id: Int($0.id), name: $0.name, organization: $0.organization)
                  }
              })
        else { return [] }
        return projects
    }
}
