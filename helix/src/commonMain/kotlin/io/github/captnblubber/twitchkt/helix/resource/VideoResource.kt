package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Video

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
     * Gets information about one or more published videos. You may get videos by ID, by user,
     * or by game/category.
     *
     * You may apply several filters to get a subset of the videos. The filters are applied as
     * an AND operation to each video. The [id], [userId], and [gameId] parameters are mutually
     * exclusive.
     *
     * @param ids a list of IDs that identify the videos you want to get. You may specify a
     * maximum of 100 IDs. The endpoint ignores duplicate IDs and IDs that weren't found.
     * @param userId the ID of the user whose list of videos you want to get.
     * @param gameId a category or game ID. The response contains a maximum of 500 videos that
     * show this content.
     * @param language a filter used to filter the list of videos by the language that the video
     * owner broadcasts in. For example, to get videos that were broadcast in German, set this
     * parameter to `de`. Specify this parameter only if you specify the [gameId] query parameter.
     * @param period a filter used to filter the list of videos by when they were published.
     * Possible values are: `all`, `day`, `month`, `week`. The default is `all`. Specify this
     * parameter only if you specify the [gameId] or [userId] query parameter.
     * @param sort the order to sort the returned videos in. Possible values are: `time` (latest
     * first), `trending` (biggest gains in viewership first), `views` (highest number of views
     * first). The default is `time`. Specify this parameter only if you specify the [gameId] or
     * [userId] query parameter.
     * @param type a filter used to filter the list of videos by the video's type. Possible
     * case-sensitive values are: `all`, `archive`, `highlight`, `upload`. The default is `all`.
     * Specify this parameter only if you specify the [gameId] or [userId] query parameter.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100. The default is 20. Specify this
     * parameter only if you specify the [gameId] or [userId] query parameter.
     * @param after the cursor used to get the next page of results. Specify this parameter only
     * if you specify the [userId] query parameter.
     * @param before the cursor used to get the previous page of results. Specify this parameter
     * only if you specify the [userId] query parameter.
     * @return the list of published videos that match the filter criteria.
     */
    suspend fun get(
        ids: List<String> = emptyList(),
        userId: String? = null,
        gameId: String? = null,
        language: String? = null,
        period: String? = null,
        sort: String? = null,
        type: String? = null,
        first: Int = 20,
        after: String? = null,
        before: String? = null,
    ): List<Video> {
        val params =
            buildList {
                ids.forEach { add("id" to it) }
                userId?.let { add("user_id" to it) }
                gameId?.let { add("game_id" to it) }
                language?.let { add("language" to it) }
                period?.let { add("period" to it) }
                sort?.let { add("sort" to it) }
                type?.let { add("type" to it) }
                add("first" to first.toString())
                after?.let { add("after" to it) }
                before?.let { add("before" to it) }
            }
        return http.get<Video>("videos", params).data
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
