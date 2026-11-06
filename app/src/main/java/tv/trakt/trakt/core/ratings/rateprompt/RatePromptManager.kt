package tv.trakt.trakt.core.ratings.rateprompt

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState

internal interface RatePromptManager {
    suspend fun checkRecentlyWatched()

    suspend fun onUserDismiss(
        media: RatePromptMedia,
        hasRated: Boolean,
    )

    suspend fun onUserSuppress()

    fun onUserInteraction(media: RatePromptMedia)

    fun observeLibraryChanges(): Flow<Unit>

    /** Drops media rated outside the prompt as soon as the rating is posted. */
    fun onMediaRated(
        mediaId: TraktId,
        mediaType: MediaType,
    )

    fun observe(): Flow<RatePromptState>

    fun clear()
}
