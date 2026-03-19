package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.Stream
import io.github.captnblubber.twitchkt.helix.model.StreamKey
import io.github.captnblubber.twitchkt.helix.model.StreamMarker
import io.github.captnblubber.twitchkt.helix.model.StreamMarkerGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Streams API resource.
 *
 * Provides methods for retrieving stream information, stream keys, followed streams,
 * and managing stream markers.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-streams">Twitch API Reference - Streams</a>
 */
class StreamResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Stream Key](https://dev.twitch.tv/docs/api/reference/#get-stream-key)
     *
     * Gets the channel's stream key.
     *
     * @param broadcasterId the ID of the broadcaster that owns the channel. The ID must match
     * the user ID in the access token.
     * @return the channel's [StreamKey].
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_STREAM_KEY)
    suspend fun getStreamKey(broadcasterId: String): StreamKey {
        http.validateScopes(TwitchScope.CHANNEL_READ_STREAM_KEY)
        return http
            .get<StreamKey>(
                "streams/key",
                listOf("broadcaster_id" to broadcasterId),
            ).requireFirst("streams/key")
    }

    /**
     * [Twitch API: Get Streams](https://dev.twitch.tv/docs/api/reference/#get-streams)
     *
     * Gets all streams matching the filter criteria.
     * Automatically paginates through all results.
     *
     * @param userIds filter by user IDs (max 100).
     * @param userLogins filter by user login names (max 100).
     * @param gameIds filter by game/category IDs (max 100).
     * @param type filter by stream type: `all`, `live`. Default: `all`.
     * @param language filter by language codes (max 100).
     * @return a [Flow] of [Stream] objects.
     */
    fun getAllStreams(
        userIds: List<String> = emptyList(),
        userLogins: List<String> = emptyList(),
        gameIds: List<String> = emptyList(),
        type: String? = null,
        language: List<String> = emptyList(),
    ): Flow<Stream> {
        val params =
            buildList {
                userIds.forEach { add("user_id" to it) }
                userLogins.forEach { add("user_login" to it) }
                gameIds.forEach { add("game_id" to it) }
                type?.let { add("type" to it) }
                language.forEach { add("language" to it) }
            }
        return http.paginate<Stream>("streams", params)
    }

    /**
     * [Twitch API: Get Streams](https://dev.twitch.tv/docs/api/reference/#get-streams)
     *
     * Gets a single page of streams matching the filter criteria.
     *
     * @param userIds filter by user IDs (max 100).
     * @param userLogins filter by user login names (max 100).
     * @param gameIds filter by game/category IDs (max 100).
     * @param type filter by stream type: `all`, `live`. Default: `all`.
     * @param language filter by language codes (max 100).
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [Stream] objects.
     */
    suspend fun getStreams(
        userIds: List<String> = emptyList(),
        userLogins: List<String> = emptyList(),
        gameIds: List<String> = emptyList(),
        type: String? = null,
        language: List<String> = emptyList(),
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Stream> {
        val params =
            buildList {
                userIds.forEach { add("user_id" to it) }
                userLogins.forEach { add("user_login" to it) }
                gameIds.forEach { add("game_id" to it) }
                type?.let { add("type" to it) }
                language.forEach { add("language" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "streams", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Get Followed Streams](https://dev.twitch.tv/docs/api/reference/#get-followed-streams)
     *
     * Gets all live streams of broadcasters that the user follows.
     * Automatically paginates through all results.
     *
     * @param userId the ID of the user whose list of followed streams you want to get. This ID must match the user ID in the access token.
     * @return a [Flow] of [Stream] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_FOLLOWS)
    fun getAllFollowedStreams(userId: String): Flow<Stream> {
        val params = listOf("user_id" to userId)
        return http
            .paginate<Stream>("streams/followed", params)
            .onStart { http.validateScopes(TwitchScope.USER_READ_FOLLOWS) }
    }

    /**
     * [Twitch API: Get Followed Streams](https://dev.twitch.tv/docs/api/reference/#get-followed-streams)
     *
     * Gets a single page of live streams of broadcasters that the user follows.
     *
     * @param userId the ID of the user whose list of followed streams you want to get. This ID must match the user ID in the access token.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 100). Null uses the API default.
     * @return a [Page] of [Stream] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_FOLLOWS)
    suspend fun getFollowedStreams(
        userId: String,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Stream> {
        http.validateScopes(TwitchScope.USER_READ_FOLLOWS)
        val params =
            buildList {
                add("user_id" to userId)
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "streams/followed", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Create Stream Marker](https://dev.twitch.tv/docs/api/reference/#create-stream-marker)
     *
     * Adds a marker to a live stream. A marker is an arbitrary point in a live stream that the
     * broadcaster or editor wants to mark, so they can return to that spot later to create video
     * highlights.
     *
     * You may not add markers if the stream is not live, has not enabled VOD, is a premiere,
     * or is a rerun of a past broadcast.
     *
     * @param userId the ID of the broadcaster that's streaming content. This ID must match the
     * user ID in the access token or the user in the access token must be one of the broadcaster's
     * editors.
     * @param description a short description of the marker to help the user remember why they
     * marked the location. The maximum length of the description is 140 characters.
     * @return the single marker that was added.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_BROADCAST)
    suspend fun createStreamMarker(
        userId: String,
        description: String? = null,
    ): StreamMarker {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_BROADCAST)
        return http
            .post<StreamMarker>(
                "streams/markers",
                body =
                    http.encodeBody(
                        CreateStreamMarkerRequest(
                            userId = userId,
                            description = description,
                        ),
                    ),
            ).requireFirst("streams/markers")
    }

    /**
     * [Twitch API: Get Stream Markers](https://dev.twitch.tv/docs/api/reference/#get-stream-markers)
     *
     * Gets all stream markers for the user or video.
     * Automatically paginates through all results.
     *
     * @param userId a user ID. The request returns the markers from this user's most recent video.
     * @param videoId a video on demand (VOD)/video ID.
     * @return a [Flow] of [StreamMarkerGroup] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST)
    fun getAllStreamMarkers(
        userId: String? = null,
        videoId: String? = null,
    ): Flow<StreamMarkerGroup> {
        val params =
            buildList {
                userId?.let { add("user_id" to it) }
                videoId?.let { add("video_id" to it) }
            }
        return http
            .paginate<StreamMarkerGroup>("streams/markers", params)
            .onStart { http.validateAnyScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST) }
    }

    /**
     * [Twitch API: Get Stream Markers](https://dev.twitch.tv/docs/api/reference/#get-stream-markers)
     *
     * Gets a single page of stream markers for the user or video.
     *
     * @param userId a user ID. The request returns the markers from this user's most recent video.
     * @param videoId a video on demand (VOD)/video ID.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [StreamMarkerGroup] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST)
    suspend fun getStreamMarkers(
        userId: String? = null,
        videoId: String? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<StreamMarkerGroup> {
        http.validateAnyScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST)
        val params =
            buildList {
                userId?.let { add("user_id" to it) }
                videoId?.let { add("video_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "streams/markers", params = params, pageSize = pageSize)
    }
}

@Serializable
internal data class CreateStreamMarkerRequest(
    @SerialName("user_id") val userId: String,
    val description: String? = null,
)
