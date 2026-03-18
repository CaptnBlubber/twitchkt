package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a channel's stream key.
 *
 * @property streamKey the channel's stream key.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-stream-key">Twitch API Reference - Get Stream Key</a>
 */
@Serializable
data class StreamKey(
    @SerialName("stream_key") val streamKey: String,
)
