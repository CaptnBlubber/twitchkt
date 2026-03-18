package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Get Global Chat Badges](https://dev.twitch.tv/docs/api/reference/#get-global-chat-badges)
 *
 * @property setId an ID that identifies this set of chat badges (e.g., `subscriber`).
 * @property versions the list of different versions of the badge within this set.
 */
@Serializable
data class ChatBadge(
    @SerialName("set_id") val setId: String,
    val versions: List<ChatBadgeVersion>,
)

/**
 * A single version of a [ChatBadge].
 *
 * @property id an ID that identifies this version of the badge. The ID can be any value. For example, for Bits, the ID is the Bits tier level, but for World of Warcraft, it could be Alliance or Horde.
 * @property imageUrl1x a URL to the small version (18px x 18px) of the badge.
 * @property imageUrl2x a URL to the medium version (36px x 36px) of the badge.
 * @property imageUrl4x a URL to the large version (72px x 72px) of the badge.
 * @property title the title of the badge.
 * @property description the description of the badge.
 * @property clickAction the action to take when clicking on the badge. `null` if no action is specified.
 * @property clickUrl the URL to navigate to when clicking on the badge. `null` if no URL is specified.
 */
@Serializable
data class ChatBadgeVersion(
    val id: String,
    @SerialName("image_url_1x") val imageUrl1x: String,
    @SerialName("image_url_2x") val imageUrl2x: String,
    @SerialName("image_url_4x") val imageUrl4x: String,
    val title: String = "",
    val description: String = "",
    @SerialName("click_action") val clickAction: String? = null,
    @SerialName("click_url") val clickUrl: String? = null,
)
