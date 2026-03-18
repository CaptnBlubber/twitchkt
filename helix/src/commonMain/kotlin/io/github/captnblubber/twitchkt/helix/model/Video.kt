package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a published video on Twitch.
 *
 * @property id an ID that identifies the video.
 * @property streamId the ID of the stream that the video originated from if the video's type
 * is `archive`; otherwise, `null`.
 * @property userId the ID of the broadcaster that owns the video.
 * @property userLogin the broadcaster's login name.
 * @property userName the broadcaster's display name.
 * @property title the video's title.
 * @property description the video's description.
 * @property createdAt the date and time, in UTC, of when the video was created.
 * @property publishedAt the date and time, in UTC, of when the video was published.
 * @property url the video's URL.
 * @property thumbnailUrl a URL to a thumbnail image of the video. Before using the URL, you
 * must replace the `%{width}` and `%{height}` placeholders with the width and height of the
 * thumbnail you want returned.
 * @property viewable the video's viewable state. Always set to `public`.
 * @property viewCount the number of times that users have watched the video.
 * @property language the ISO 639-1 two-letter language code that the video was broadcast in.
 * The language value is `other` if the video was broadcast in a language not in the list of
 * supported languages.
 * @property type the video's type. Possible values are: `archive` (VOD of a past stream),
 * `highlight` (highlight reel of a past stream), `upload` (externally uploaded video).
 * @property duration the video's length in ISO 8601 duration format. For example, `3m21s`
 * represents 3 minutes, 21 seconds.
 * @property mutedSegments the segments that Twitch Audio Recognition muted; otherwise, `null`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-videos">Twitch API Reference - Get Videos</a>
 */
@Serializable
data class Video(
    val id: String,
    @SerialName("stream_id") val streamId: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val title: String,
    val description: String = "",
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("published_at") val publishedAt: Instant,
    val url: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val viewable: String = "public",
    @SerialName("view_count") val viewCount: Int = 0,
    val language: String = "",
    val type: String = "",
    val duration: String = "",
    @SerialName("muted_segments") val mutedSegments: List<MutedSegment>? = null,
)

/**
 * Represents a muted segment within a video, as detected by Twitch Audio Recognition.
 *
 * @property duration the duration of the muted segment, in seconds.
 * @property offset the offset, in seconds, from the beginning of the video to where the
 * muted segment begins.
 */
@Serializable
data class MutedSegment(
    val duration: Int,
    val offset: Int,
)
