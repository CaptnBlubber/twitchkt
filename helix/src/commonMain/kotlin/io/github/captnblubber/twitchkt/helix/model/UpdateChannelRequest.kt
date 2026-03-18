package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Modify Channel Information](https://dev.twitch.tv/docs/api/reference/#modify-channel-information)
 *
 * @property gameId the ID of the game that the user plays; use `"0"` or `""` to unset.
 * @property broadcasterLanguage the user's preferred language as an ISO 639-1 two-letter code.
 * @property title the title of the user's stream.
 * @property delay the number of seconds to buffer the broadcast before streaming it live; Partners only, max 900.
 * @property tags a list of channel-defined tags; set to an empty array to remove all tags.
 * @property contentClassificationLabels a list of content classification labels to apply to the channel.
 * @property isBrandedContent whether the channel has branded content.
 */
@Serializable
data class UpdateChannelRequest(
    @SerialName("game_id") val gameId: String? = null,
    @SerialName("broadcaster_language") val broadcasterLanguage: String? = null,
    val title: String? = null,
    val delay: Int? = null,
    val tags: List<String>? = null,
    @SerialName("content_classification_labels") val contentClassificationLabels: List<ContentClassificationLabel>? = null,
    @SerialName("is_branded_content") val isBrandedContent: Boolean? = null,
)

/**
 * A content classification label for [UpdateChannelRequest].
 *
 * @property id the ID of the content classification label.
 * @property isEnabled whether the label is enabled.
 */
@Serializable
data class ContentClassificationLabel(
    val id: String,
    @SerialName("is_enabled") val isEnabled: Boolean,
)
