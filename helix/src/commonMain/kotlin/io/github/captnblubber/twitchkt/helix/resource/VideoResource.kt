package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Video
import kotlinx.coroutines.flow.Flow

/**
 * Twitch Helix Videos API resource.
 *
 * Provides methods for retrieving and deleting videos.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-videos">Twitch API Reference - Videos</a>
 */
class VideoResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Videos](https://dev.twitch.tv/docs/api/reference/#get-videos)
     *
     * Gets all published videos matching the filter criteria.
     * Automatically paginates through all results.
     *
     * @param ids a list of IDs that identify the videos you want to get. You may specify a maximum of 100 IDs.
     * @param userId the ID of the user whose list of videos you want to get.
     * @param gameId a category or game ID.
     * @param language a filter used to filter the list of videos by the language that the video owner broadcasts in.
     * @param period a filter used to filter the list of videos by when they were published. Possible values: `all`, `day`, `month`, `week`. Default: `all`.
     * @param sort the order to sort the returned videos in. Possible values: `time`, `trending`, `views`. Default: `time`.
     * @param type a filter used to filter the list of videos by the video's type. Possible values: `all`, `archive`, `highlight`, `upload`. Default: `all`.
     * @return a [Flow] of [Video] objects.
     */
    fun getAllVideos(
        ids: List<String> = emptyList(),
        userId: String? = null,
        gameId: String? = null,
        language: String? = null,
        period: String? = null,
        sort: String? = null,
        type: String? = null,
    ): Flow<Video> {
        val params =
            buildList {
                ids.forEach { add("id" to it) }
                userId?.let { add("user_id" to it) }
                gameId?.let { add("game_id" to it) }
                language?.let { add("language" to it) }
                period?.let { add("period" to it) }
                sort?.let { add("sort" to it) }
                type?.let { add("type" to it) }
            }
        return http.paginate<Video>("videos", params)
    }

    /**
     * [Twitch API: Get Videos](https://dev.twitch.tv/docs/api/reference/#get-videos)
     *
     * Gets a single page of published videos matching the filter criteria.
     *
     * @param ids a list of IDs that identify the videos you want to get. You may specify a maximum of 100 IDs.
     * @param userId the ID of the user whose list of videos you want to get.
     * @param gameId a category or game ID.
     * @param language a filter used to filter the list of videos by the language that the video owner broadcasts in.
     * @param period a filter used to filter the list of videos by when they were published. Possible values: `all`, `day`, `month`, `week`. Default: `all`.
     * @param sort the order to sort the returned videos in. Possible values: `time`, `trending`, `views`. Default: `time`.
     * @param type a filter used to filter the list of videos by the video's type. Possible values: `all`, `archive`, `highlight`, `upload`. Default: `all`.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [Video] objects.
     */
    suspend fun get(
        ids: List<String> = emptyList(),
        userId: String? = null,
        gameId: String? = null,
        language: String? = null,
        period: String? = null,
        sort: String? = null,
        type: String? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Video> {
        val params =
            buildList {
                ids.forEach { add("id" to it) }
                userId?.let { add("user_id" to it) }
                gameId?.let { add("game_id" to it) }
                language?.let { add("language" to it) }
                period?.let { add("period" to it) }
                sort?.let { add("sort" to it) }
                type?.let { add("type" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "videos", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Delete Videos](https://dev.twitch.tv/docs/api/reference/#delete-videos)
     *
     * Deletes one or more videos. You may delete past broadcasts, highlights, or uploads.
     *
     * If the user doesn't have permission to delete one of the videos in the list, none of
     * the videos are deleted.
     *
     * @param ids the list of videos to delete. You can delete a maximum of 5 videos per request.
     * Ignores invalid video IDs.
     * @return the list of IDs of the videos that were deleted.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_VIDEOS)
    suspend fun delete(ids: List<String>): List<String> {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_VIDEOS)
        val params = ids.map { "id" to it }
        return http.delete<String>("videos", params).data
    }
}
