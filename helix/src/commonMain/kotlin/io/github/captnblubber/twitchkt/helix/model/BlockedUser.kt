package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user that the broadcaster has blocked.
 *
 * @property userId the ID of the blocked user.
 * @property userLogin the blocked user's login name.
 * @property displayName the blocked user's display name.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-user-block-list">Twitch API Reference - Get User Block List</a>
 */
@Serializable
data class BlockedUser(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("display_name") val displayName: String,
)
