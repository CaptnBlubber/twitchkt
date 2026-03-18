package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a stream marker created via the Twitch Helix API.
 *
 * @property id an ID that identifies this marker.
 * @property createdAt the UTC date and time of when the user created the marker.
 * @property positionSeconds the relative offset (in seconds) of the marker from the beginning
 * of the stream.
 * @property description a description that the user gave the marker to help them remember why
 * they marked the location. Is an empty string if the user didn't provide one.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#create-stream-marker">Twitch API Reference - Create Stream Marker</a>
 */
@Serializable
data class StreamMarker(
    val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("position_seconds") val positionSeconds: Int,
    val description: String = "",
)

/**
 * Represents a group of stream markers for a user, as returned by Get Stream Markers.
 *
 * @property userId the ID of the user that created the marker.
 * @property userName the user's display name.
 * @property userLogin the user's login name.
 * @property videos a list of videos that contain markers. The list contains a single video.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-stream-markers">Twitch API Reference - Get Stream Markers</a>
 */
@Serializable
data class StreamMarkerGroup(
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    @SerialName("user_login") val userLogin: String,
    val videos: List<StreamMarkerVideo> = emptyList(),
)

/**
 * Represents a video containing stream markers.
 *
 * @property videoId an ID that identifies this video.
 * @property markers the list of markers in this video. The list is in ascending order by when
 * the marker was created.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-stream-markers">Twitch API Reference - Get Stream Markers</a>
 */
@Serializable
data class StreamMarkerVideo(
    @SerialName("video_id") val videoId: String,
    val markers: List<StreamMarkerDetail> = emptyList(),
)

/**
 * Represents a detailed stream marker within a video, including a URL to the Twitch Highlighter.
 *
 * @property id an ID that identifies this marker.
 * @property createdAt the UTC date and time of when the user created the marker.
 * @property description the description that the user gave the marker to help them remember why
 * they marked the location. Is an empty string if the user didn't provide one.
 * @property positionSeconds the relative offset (in seconds) of the marker from the beginning
 * of the stream.
 * @property url a URL that opens the video in Twitch Highlighter.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-stream-markers">Twitch API Reference - Get Stream Markers</a>
 */
@Serializable
data class StreamMarkerDetail(
    val id: String,
    @SerialName("created_at") val createdAt: Instant,
    val description: String = "",
    @SerialName("position_seconds") val positionSeconds: Int,
    @SerialName("URL") val url: String = "",
)
