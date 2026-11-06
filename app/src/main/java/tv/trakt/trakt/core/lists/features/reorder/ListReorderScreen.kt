package tv.trakt.trakt.core.lists.features.reorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderActions
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderItem
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderLayout
import tv.trakt.trakt.core.lists.features.reorder.ui.ReorderUiState

@Composable
internal fun ListReorderScreen(
    modifier: Modifier = Modifier,
    viewModel: ListReorderViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val uiState = remember(state) {
        ReorderUiState(
            title = state.list?.name.orEmpty(),
            items = state.items
                ?.map { item ->
                    ReorderItem(
                        key = item.key,
                        title = item.title,
                        subtitle = item.released?.toLocal()?.year?.toString().orEmpty(),
                        posterUrl = item.images?.getPosterUrl(),
                        backdropUrl = item.images?.getFanartUrl(Images.Size.THUMB),
                    )
                }
                ?.toImmutableList(),
            changed = state.items != null &&
                state.items?.map { it.itemId } != state.initialItemsOrder,
            loading = state.loading.isLoading,
            error = state.error,
            done = state.done,
        )
    }

    val actions = remember(viewModel) {
        ReorderActions(
            onMove = viewModel::reorderItem,
            onMoveToTop = viewModel::moveToTop,
            onMoveToBottom = viewModel::moveToBottom,
            onMoveToPosition = { index, position ->
                viewModel.moveToPosition(index = index, position = position)
            },
            onApply = viewModel::applyChanges,
            onErrorShown = viewModel::clearError,
        )
    }

    ReorderLayout(
        state = uiState,
        actions = actions,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}
