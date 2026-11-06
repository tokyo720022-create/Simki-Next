package tv.trakt.trakt.core.calendar.ui.context

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CalendarEpisodeContextState(
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val loadingWatched: LoadingState = Idle,
    val loadingWatchlist: LoadingState = Idle,
    val loadingDrop: LoadingState = Idle,
    val user: User? = null,
    val error: Exception? = null,
)
