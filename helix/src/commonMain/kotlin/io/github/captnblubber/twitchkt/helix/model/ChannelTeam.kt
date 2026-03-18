package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a team that a broadcaster is a member of, as returned by Get Channel Teams.
 *
 * @property broadcasterId an ID that identifies the broadcaster.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property backgroundImageUrl a URL to the team's background image.
 * @property banner a URL to the team's banner.
 * @property createdAt the UTC date and time of when the team was created.
 * @property updatedAt the UTC date and time of the last time the team was updated.
 * @property info the team's description. The description may contain formatting such as
 * Markdown, HTML, newline (`\n`) characters, etc.
 * @property thumbnailUrl a URL to a thumbnail image of the team's logo.
 * @property teamDisplayName the team's display name.
 * @property teamName the team's name.
 * @property id an ID that identifies the team.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-teams">Twitch API Reference - Get Channel Teams</a>
 */
@Serializable
data class ChannelTeam(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("background_image_url") val backgroundImageUrl: String? = null,
    val banner: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val info: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("team_display_name") val teamDisplayName: String,
    @SerialName("team_name") val teamName: String,
    val id: String,
)
