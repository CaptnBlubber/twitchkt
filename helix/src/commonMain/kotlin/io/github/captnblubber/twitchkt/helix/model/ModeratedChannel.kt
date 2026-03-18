package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a channel that a user has moderator privileges in.
 *
 * @property broadcasterId an ID that uniquely identifies the channel this user can moderate.
 * @property broadcasterLogin the channel's login name.
 * @property broadcasterName the channel's display name.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-moderated-channels">Twitch API Reference - Get Moderated Channels</a>
 */
@Serializable
data class ModeratedChannel(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
)
