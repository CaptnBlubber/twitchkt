package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a channel editor from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-editors">Twitch API Reference - Get Channel Editors</a>
 */
@Serializable
data class ChannelEditor(
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    @SerialName("created_at") val createdAt: Instant,
)

/**
 * Represents a followed channel from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-followed-channels">Twitch API Reference - Get Followed Channels</a>
 */
@Serializable
data class FollowedChannel(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("followed_at") val followedAt: Instant,
)
