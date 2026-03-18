package io.github.captnblubber.twitchkt.helix.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.Stream
import io.github.captnblubber.twitchkt.helix.model.StreamKey
import io.github.captnblubber.twitchkt.helix.model.StreamMarker
import io.github.captnblubber.twitchkt.helix.model.StreamMarkerGroup

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
     * Gets a list of all streams. The list is in descending order by the number of viewers
     * watching the stream. Because viewers come and go during a stream, it's possible to find
     * duplicate or missing streams in the list as you page through the results.
     *
     * @param userIds a user ID used to filter the list of streams. Returns only the streams of
     * those users that are broadcasting. You may specify a maximum of 100 IDs.
     * @param userLogins a user login name used to filter the list of streams. Returns only the
     * streams of those users that are broadcasting. You may specify a maximum of 100 login names.
     * @param gameIds a game (category) ID used to filter the list of streams. Returns only the
     * streams that are broadcasting the game (category). You may specify a maximum of 100 IDs.
     * @param type the type of stream to filter the list of streams by. Possible values are:
     * `all`, `live`. The default is `all`.
     * @param language a language code used to filter the list of streams. Returns only streams
     * that broadcast in the specified language. Specify the language using an ISO 639-1 two-letter
     * language code or `other`. You may specify a maximum of 100 language codes.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param before the cursor used to get the previous page of results.
     * @param after the cursor used to get the next page of results.
     * @return the list of streams.
     */
    suspend fun getStreams(
        userIds: List<String> = emptyList(),
        userLogins: List<String> = emptyList(),
        gameIds: List<String> = emptyList(),
        type: String? = null,
        language: List<String> = emptyList(),
        first: Int = 20,
        before: String? = null,
        after: String? = null,
    ): List<Stream> {
        val params =
            buildList {
                userIds.forEach { add("user_id" to it) }
                userLogins.forEach { add("user_login" to it) }
                gameIds.forEach { add("game_id" to it) }
                type?.let { add("type" to it) }
                language.forEach { add("language" to it) }
                add("first" to first.toString())
                before?.let { add("before" to it) }
                after?.let { add("after" to it) }
            }
        return http.get<Stream>("streams", params).data
    }

    /**
     * [Twitch API: Get Followed Streams](https://dev.twitch.tv/docs/api/reference/#get-followed-streams)
     *
     * Gets the list of broadcasters that the user follows and that are streaming live.
     *
     * @param userId the ID of the user whose list of followed streams you want to get. This ID
     * must match the user ID in the access token.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 100.
     * @param after the cursor used to get the next page of results.
     * @return the list of live streams of broadcasters that the specified user follows. The list
     * is in descending order by the number of viewers watching the stream. The list is empty if
     * none of the followed broadcasters are streaming live.
     */
    @RequiresScope(TwitchScope.USER_READ_FOLLOWS)
    suspend fun getFollowedStreams(
        userId: String,
        first: Int = 100,
        after: String? = null,
    ): List<Stream> {
        http.validateScopes(TwitchScope.USER_READ_FOLLOWS)
        val params =
            buildList {
                add("user_id" to userId)
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<Stream>("streams/followed", params).data
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
     * Gets a list of markers from the user's most recent stream or from the specified VOD/video.
     * A marker is an arbitrary point in a live stream that the broadcaster or editor marked, so
     * they can return to that spot later to create video highlights.
     *
     * The [userId] and [videoId] parameters are mutually exclusive.
     *
     * @param userId a user ID. The request returns the markers from this user's most recent video.
     * This ID must match the user ID in the access token or the user in the access token must be
     * one of the broadcaster's editors.
     * @param videoId a video on demand (VOD)/video ID. The request returns the markers from this
     * VOD/video. The user in the access token must own the video or the user must be one of the
     * broadcaster's editors.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param before the cursor used to get the previous page of results.
     * @param after the cursor used to get the next page of results.
     * @return the list of markers grouped by the user that created the marks.
     */
    @RequiresScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST)
    suspend fun getStreamMarkers(
        userId: String? = null,
        videoId: String? = null,
        first: Int = 20,
        before: String? = null,
        after: String? = null,
    ): List<StreamMarkerGroup> {
        http.validateAnyScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.CHANNEL_MANAGE_BROADCAST)
        val params =
            buildList {
                userId?.let { add("user_id" to it) }
                videoId?.let { add("video_id" to it) }
                add("first" to first.toString())
                before?.let { add("before" to it) }
                after?.let { add("after" to it) }
            }
        return http.get<StreamMarkerGroup>("streams/markers", params).data
    }
}

@Serializable
internal data class CreateStreamMarkerRequest(
    @SerialName("user_id") val userId: String,
    val description: String? = null,
)
