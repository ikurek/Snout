package com.nightcode.snout.features.widgets

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.AppTheme
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.widgets.ProjectChoice
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The insight picker an Android insight widget opens when it is added or reconfigured, in the app's theme. Not a
 * destination of the app: the widget's configuration activity shows it on its own.
 */
@Composable
fun InsightWidgetPicker(
    projectId: Long?,
    onInsightPicked: (project: ProjectChoice, insightId: Long, name: String) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<InsightWidgetPickerViewModel> { parametersOf(WidgetProject(projectId)) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppTheme(initialThemeMode = ThemeMode.System) {
        Surface {
            InsightWidgetPickerScreen(
                state = state,
                onProjectSelected = viewModel::onProjectSelected,
                onRetryProjects = viewModel::retryProjects,
                onQueryChanged = viewModel::onQueryChanged,
                onRefresh = viewModel::refresh,
                // The screen lists insights only once their project is among the projects.
                onInsightPicked = { insight ->
                    state.projects?.firstOrNull { it.id == state.projectId }
                        ?.let { onInsightPicked(it, insight.id, insight.name) }
                },
                onClose = onClose,
            )
        }
    }
}
