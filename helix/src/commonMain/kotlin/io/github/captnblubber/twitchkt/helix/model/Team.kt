package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a Twitch team with its members, as returned by Get Teams.
 *
 * @property users the list of team members.
 * @property backgroundImageUrl a URL to the team's background image.
 * @property banner a URL to the team's banner.
 * @property createdAt the UTC date and time of when the team was created.
 * @property updatedAt the UTC date and time of the last time the team was updated.
 * @property info the team's description. The description may contain formatting such as
 * Markdown, HTML, newline (`\n`) characters, etc.
 * @property thumbnailUrl a URL to a thumbnail image of the team's logo.
 * @property teamName the team's name.
 * @property teamDisplayName the team's display name.
 * @property id an ID that identifies the team.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-teams">Twitch API Reference - Get Teams</a>
 */
@Serializable
data class Team(
    val users: List<TeamMember> = emptyList(),
    @SerialName("background_image_url") val backgroundImageUrl: String? = null,
    val banner: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val info: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("team_name") val teamName: String,
    @SerialName("team_display_name") val teamDisplayName: String,
    val id: String,
)

/**
 * Represents a member of a Twitch team.
 *
 * @property userId an ID that identifies the team member.
 * @property userLogin the team member's login name.
 * @property userName the team member's display name.
 */
@Serializable
data class TeamMember(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
)
