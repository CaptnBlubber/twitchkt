package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a live stream on Twitch.
 *
 * @property id an ID that identifies the stream. You can use this ID later to look up the
 * video on demand (VOD).
 * @property userId the ID of the user that's broadcasting the stream.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property gameId the ID of the category or game being played.
 * @property gameName the name of the category or game being played.
 * @property type the type of stream. Possible values are: `live`. If an error occurs, this
 * field is set to an empty string.
 * @property title the stream's title. Is an empty string if not set.
 * @property tags the tags applied to the stream.
 * @property viewerCount the number of users watching the stream.
 * @property startedAt the UTC date and time of when the broadcast began.
 * @property language the language that the stream uses. This is an ISO 639-1 two-letter
 * language code or `other` if the stream uses a language not in the list of supported stream
 * languages.
 * @property thumbnailUrl a URL to an image of a frame from the last 5 minutes of the stream.
 * Replace the width and height placeholders in the URL (`{width}x{height}`) with the size of
 * the image you want, in pixels.
 * @property isMature **Deprecated.** This field is deprecated and returns only `false`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-streams">Twitch API Reference - Get Streams</a>
 */
@Serializable
data class Stream(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("game_id") val gameId: String = "",
    @SerialName("game_name") val gameName: String = "",
    val type: String = "",
    val title: String = "",
    val tags: List<String>? = null,
    @SerialName("viewer_count") val viewerCount: Int = 0,
    @SerialName("started_at") val startedAt: Instant? = null,
    val language: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String = "",
    @Deprecated("This field is deprecated and returns only false.")
    @SerialName("is_mature") val isMature: Boolean = false,
)
