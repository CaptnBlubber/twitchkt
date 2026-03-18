package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * [Twitch API: Clip](https://dev.twitch.tv/docs/api/reference/#get-clips)
 *
 * @property id the clip ID.
 * @property url the clip URL.
 * @property embedUrl the embeddable clip URL.
 * @property broadcasterId the broadcaster's user ID.
 * @property broadcasterName the broadcaster's display name.
 * @property creatorId the user ID of the clip creator.
 * @property creatorName the display name of the clip creator.
 * @property videoId the VOD ID the clip was created from; empty if the VOD has been deleted.
 * @property gameId the game/category ID.
 * @property language the language of the stream when the clip was created.
 * @property title the clip title.
 * @property viewCount the number of times the clip has been viewed.
 * @property createdAt when the clip was created.
 * @property thumbnailUrl the clip thumbnail URL.
 * @property duration the clip duration in seconds.
 * @property vodOffset the offset in the VOD where the clip starts; `null` if unknown.
 * @property isFeatured whether the clip is featured.
 */
@Serializable
data class Clip(
    val id: String,
    val url: String,
    @SerialName("embed_url") val embedUrl: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("creator_name") val creatorName: String,
    @SerialName("video_id") val videoId: String = "",
    @SerialName("game_id") val gameId: String = "",
    val language: String = "",
    val title: String,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val duration: Double = 0.0,
    @SerialName("vod_offset") val vodOffset: Int? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false,
)

/**
 * Response from creating a clip.
 *
 * @property id the clip ID.
 * @property editUrl the URL to edit the clip.
 */
@Serializable
data class CreatedClip(
    val id: String,
    @SerialName("edit_url") val editUrl: String,
)

/**
 * [Twitch API: Get Clips Download](https://dev.twitch.tv/docs/api/reference/#get-clips-download)
 *
 * @property clipId the clip ID.
 * @property landscapeDownloadUrl the landscape download URL, or `null` if not available.
 * @property portraitDownloadUrl the portrait download URL, or `null` if not available.
 */
@Serializable
data class ClipDownload(
    @SerialName("clip_id") val clipId: String,
    @SerialName("landscape_download_url") val landscapeDownloadUrl: String? = null,
    @SerialName("portrait_download_url") val portraitDownloadUrl: String? = null,
)
