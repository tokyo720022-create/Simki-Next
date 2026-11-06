package tv.trakt.trakt.app.core.lists.details.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie

@Suppress("UNCHECKED_CAST")
internal class MoviesWatchlistViewModel(
    collectionStateProvider: CollectionStateProvider,
    private val getListItemsUseCase: GetListsMoviesWatchlistUseCase,
) : ViewModel() {
    private val initialState = MoviesWatchlistState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val moviesState = MutableStateFlow(initialState.movies)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()
        collectionStateProvider.launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val movies = getListItemsUseCase.getMovies(LISTS_PAGE_LIMIT)
                moviesState.update { movies }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val movies = getListItemsUseCase.getMovies(
                    limit = LISTS_PAGE_LIMIT,
                    page = nextDataPage,
                )

                moviesState.update { current ->
                    current
                        ?.toPersistentList()
                        ?.plus(movies)
                        ?.distinctBy { it.ids.trakt }
                        ?.toImmutableList()
                }

                hasMoreData = (movies.size >= LISTS_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        moviesState,
        collectionStateProvider.stateFlow.map { collection ->
            collection
                .watchlistCount(MediaMode.Movies)
                .takeIf { it > 0 }
        },
        errorState,
    ) { state ->
        MoviesWatchlistState(
            isLoading = state[0] as Boolean,
            isLoadingPage = state[1] as Boolean,
            movies = state[2] as ImmutableList<Movie>?,
            itemCount = state[3] as Int?,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
