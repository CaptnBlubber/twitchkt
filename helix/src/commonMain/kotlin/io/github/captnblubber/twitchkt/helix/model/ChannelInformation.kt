package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Get Channel Information](https://dev.twitch.tv/docs/api/reference/#get-channel-information)
 *
 * @property broadcasterId an ID that uniquely identifies the broadcaster.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property broadcasterLanguage the broadcaster's preferred language as an ISO 639-1 two-letter code.
 * @property gameName the name of the game the broadcaster is playing or last played.
 * @property gameId an ID that uniquely identifies the game the broadcaster is playing or last played.
 * @property title the title of the stream the broadcaster is currently streaming or last streamed.
 * @property delay the value of the broadcaster's stream delay setting, in seconds.
 * @property tags the tags applied to the channel.
 * @property contentClassificationLabels the content classification labels applied to the channel.
 * @property isBrandedContent whether the channel has branded content.
 */
@Serializable
data class ChannelInformation(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("broadcaster_language") val broadcasterLanguage: String = "",
    @SerialName("game_name") val gameName: String = "",
    @SerialName("game_id") val gameId: String = "",
    val title: String = "",
    val delay: Int = 0,
    val tags: List<String>? = null,
    @SerialName("content_classification_labels") val contentClassificationLabels: List<String> = emptyList(),
    @SerialName("is_branded_content") val isBrandedContent: Boolean = false,
)
