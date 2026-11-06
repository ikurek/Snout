package com.nightcode.snout.features.persons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.AvatarBox
import com.nightcode.snout.designsystem.atoms.textfields.SearchField
import com.nightcode.snout.designsystem.molecules.EntityRow
import com.nightcode.snout.designsystem.organisms.LoadingOrErrorContent
import com.nightcode.snout.designsystem.organisms.NoSearchResultsPlaceholder
import com.nightcode.snout.designsystem.organisms.NothingFoundPlaceholder
import com.nightcode.snout.designsystem.organisms.RefreshableContent
import com.nightcode.snout.designsystem.organisms.RefreshableEmptyContent
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.features.persons.components.PersonCreatedMeta
import com.nightcode.snout.features.persons.components.secondaryLabel
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.persons.Person
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.persons_search_empty_title
import snout.composeapp.generated.resources.persons_search_placeholder

@Composable
internal fun PersonsScreen(
    state: PersonsUiState,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenPerson: (uuid: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val searchState = rememberTextFieldState(state.query)
    LaunchedEffect(searchState.text) { onQueryChanged(searchState.text.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            state = searchState,
            placeholder = stringResource(Res.string.persons_search_placeholder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large, vertical = Spacing.small),
        )
        val isSearching = state.query.isNotBlank()
        val persons = if (isSearching) state.searchResults else state.persons
        val error = if (isSearching) state.searchError else state.error
        val isRefreshing = !isSearching && state.isRefreshing
        when {
            persons == null -> LoadingOrErrorContent(error = error, onRetry = onRefresh)

            persons.isEmpty() -> RefreshableEmptyContent(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                if (isSearching) {
                    NoSearchResultsPlaceholder(
                        title = stringResource(Res.string.persons_search_empty_title),
                        onClearSearch = { searchState.clearText() },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    NothingFoundPlaceholder(onRetry = onRefresh, modifier = Modifier.fillMaxSize())
                }
            }

            else -> RefreshableContent(isRefreshing = isRefreshing, refreshError = error, onRefresh = onRefresh) {
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.large) + contentPadding) {
                    itemsIndexed(persons, key = { _, person -> person.uuid }) { index, person ->
                        PersonRow(
                            person = person,
                            divider = index < persons.lastIndex,
                            onClick = { onOpenPerson(person.uuid) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonRow(person: Person, divider: Boolean, onClick: () -> Unit) {
    EntityRow(
        title = person.displayName,
        titleMaxLines = 1,
        subtitle = person.secondaryLabel,
        divider = divider,
        onClick = onClick,
        leading = { AvatarBox(name = person.displayName) },
        meta = person.createdAt?.let { createdAt -> { PersonCreatedMeta(createdAt) } },
    )
}

@OptIn(TestData::class)
@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonsScreenPreview() = PersonsScreen(
    state = TestPersonsUiState,
    onQueryChanged = {},
    onRefresh = {},
    onOpenPerson = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonsScreenNoResultsPreview() = PersonsScreen(
    state = PersonsUiState(persons = emptyList(), query = "nobody@example.com", searchResults = emptyList()),
    onQueryChanged = {},
    onRefresh = {},
    onOpenPerson = {},
)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PersonsScreenForbiddenPreview() = PersonsScreen(
    state = PersonsUiState(error = DataError.Forbidden),
    onQueryChanged = {},
    onRefresh = {},
    onOpenPerson = {},
)
