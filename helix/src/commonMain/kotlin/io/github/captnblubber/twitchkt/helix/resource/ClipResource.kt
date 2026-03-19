package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.Clip
import io.github.captnblubber.twitchkt.helix.model.ClipDownload
import io.github.captnblubber.twitchkt.helix.model.CreatedClip
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Twitch Helix Clips API resource.
 *
 * Provides methods for creating and retrieving video clips.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#create-clip">Twitch API Reference - Clips</a>
 */
class ClipResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Create Clip](https://dev.twitch.tv/docs/api/reference/#create-clip)
     *
     * Creates a clip from the broadcaster's stream. This API captures up to 90 seconds of the
     * broadcaster's stream. By default, Twitch publishes up to the last 30 seconds of the 90
     * seconds window and provides a default title for the clip.
     *
     * Creating a clip is an asynchronous process. To determine whether the clip was successfully
     * created, call [get] using the clip ID that this request returned. If after 15 seconds
     * [get] hasn't returned the clip, assume it failed.
     *
     * @param broadcasterId the ID of the broadcaster whose stream you want to create a clip from.
     * @param title the title of the clip.
     * @param duration the length of the clip in seconds. Possible values range from 5 to 60
     * inclusively with a precision of 0.1. The default is 30.
     * @return the created clip info containing the clip ID and edit URL.
     */
    @RequiresScope(TwitchScope.CLIPS_EDIT)
    suspend fun create(
        broadcasterId: String,
        title: String? = null,
        duration: Double? = null,
    ): CreatedClip {
        http.validateScopes(TwitchScope.CLIPS_EDIT)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                title?.let { add("title" to it) }
                duration?.let { add("duration" to it.toString()) }
            }
        return http.post<CreatedClip>("clips", params = params).requireFirst("clips")
    }

    /**
     * [Twitch API: Create Clip From VOD](https://dev.twitch.tv/docs/api/reference/#create-clip-from-vod)
     *
     * Creates a clip from a broadcaster's VOD on behalf of the broadcaster or an editor of the
     * channel. Since a live stream is actively creating a VOD, this endpoint can also be used to
     * create a clip from earlier in the current stream.
     *
     * `vod_offset` indicates where the clip will end. The clip will start at
     * (`vodOffset` - `duration`) and end at `vodOffset`. This means that [vodOffset] must be
     * greater than or equal to [duration].
     *
     * @param editorId the user ID of the editor for the channel. If using the broadcaster's auth
     * token, this is the same as [broadcasterId]. Must match the user_id in the access token.
     * @param broadcasterId the user ID for the channel you want to create a clip for.
     * @param vodId the ID of the VOD to clip.
     * @param vodOffset the offset in the VOD (in seconds) where the clip ends.
     * @param title the title of the clip.
     * @param duration the length of the clip in seconds. Precision is 0.1. Defaults to 30.
     * Min: 5 seconds, Max: 60 seconds.
     * @return the created clip info containing the clip ID and edit URL.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_CLIPS, TwitchScope.EDITOR_MANAGE_CLIPS)
    suspend fun createFromVod(
        editorId: String,
        broadcasterId: String,
        vodId: String,
        vodOffset: Int,
        title: String,
        duration: Double? = null,
    ): CreatedClip {
        http.validateAnyScope(TwitchScope.CHANNEL_MANAGE_CLIPS, TwitchScope.EDITOR_MANAGE_CLIPS)
        val params =
            buildList {
                add("editor_id" to editorId)
                add("broadcaster_id" to broadcasterId)
                add("vod_id" to vodId)
                add("vod_offset" to vodOffset.toString())
                add("title" to title)
                duration?.let { add("duration" to it.toString()) }
            }
        return http.post<CreatedClip>("videos/clips", params = params).requireFirst("videos/clips")
    }

    /**
     * [Twitch API: Get Clips](https://dev.twitch.tv/docs/api/reference/#get-clips)
     *
     * Gets all clips matching the filter criteria.
     * Automatically paginates through all results.
     *
     * @param broadcasterId an ID that identifies the broadcaster whose video clips you want to get.
     * @param gameId an ID that identifies the game whose clips you want to get.
     * @param ids clip IDs to get (max 100).
     * @param startedAt the start date used to filter clips.
     * @param endedAt the end date used to filter clips.
     * @param isFeatured if `true`, returns only featured clips. If `false`, only non-featured. If `null`, all clips.
     * @return a [Flow] of [Clip] objects.
     */
    fun getAllClips(
        broadcasterId: String? = null,
        gameId: String? = null,
        ids: List<String> = emptyList(),
        startedAt: Instant? = null,
        endedAt: Instant? = null,
        isFeatured: Boolean? = null,
    ): Flow<Clip> {
        val params =
            buildList {
                broadcasterId?.let { add("broadcaster_id" to it) }
                gameId?.let { add("game_id" to it) }
                ids.forEach { add("id" to it) }
                startedAt?.let { add("started_at" to it.toString()) }
                endedAt?.let { add("ended_at" to it.toString()) }
                isFeatured?.let { add("is_featured" to it.toString()) }
            }
        return http.paginate<Clip>("clips", params)
    }

    /**
     * [Twitch API: Get Clips](https://dev.twitch.tv/docs/api/reference/#get-clips)
     *
     * Gets a single page of clips matching the filter criteria.
     *
     * @param broadcasterId an ID that identifies the broadcaster whose video clips you want to get.
     * @param gameId an ID that identifies the game whose clips you want to get.
     * @param ids clip IDs to get (max 100).
     * @param startedAt the start date used to filter clips.
     * @param endedAt the end date used to filter clips.
     * @param isFeatured if `true`, returns only featured clips. If `false`, only non-featured. If `null`, all clips.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of clips to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [Clip] objects.
     */
    suspend fun get(
        broadcasterId: String? = null,
        gameId: String? = null,
        ids: List<String> = emptyList(),
        startedAt: Instant? = null,
        endedAt: Instant? = null,
        isFeatured: Boolean? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Clip> {
        val params =
            buildList {
                broadcasterId?.let { add("broadcaster_id" to it) }
                gameId?.let { add("game_id" to it) }
                ids.forEach { add("id" to it) }
                startedAt?.let { add("started_at" to it.toString()) }
                endedAt?.let { add("ended_at" to it.toString()) }
                isFeatured?.let { add("is_featured" to it.toString()) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "clips", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Get Clips Download](https://dev.twitch.tv/docs/api/reference/#get-clips-download)
     *
     * Provides URLs to download the video file(s) for the specified clips.
     *
     * Rate Limits: Limited to 100 requests per minute.
     *
     * @param editorId the user ID of the editor requesting the downloads. If using the
     * broadcaster's auth token, this is the same as [broadcasterId]. Must match the user_id
     * in the access token.
     * @param broadcasterId the ID of the broadcaster you want to download clips for.
     * @param clipIds the clip IDs to get download URLs for. You may specify up to 10 clips.
     * @return the list of clip download info with landscape and portrait URLs.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_CLIPS, TwitchScope.EDITOR_MANAGE_CLIPS)
    suspend fun getDownloadUrls(
        editorId: String,
        broadcasterId: String,
        clipIds: List<String>,
    ): List<ClipDownload> {
        http.validateAnyScope(TwitchScope.CHANNEL_MANAGE_CLIPS, TwitchScope.EDITOR_MANAGE_CLIPS)
        val params =
            buildList {
                add("editor_id" to editorId)
                add("broadcaster_id" to broadcasterId)
                clipIds.forEach { add("clip_id" to it) }
            }
        return http.get<ClipDownload>("clips/downloads", params).data
    }
}
