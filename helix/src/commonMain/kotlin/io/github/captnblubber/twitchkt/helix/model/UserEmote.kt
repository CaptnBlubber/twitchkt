package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an emote from a user's emote list.
 *
 * @property id an ID that uniquely identifies this emote.
 * @property name the name of the emote. This is the name that viewers type in the chat window to get the emote to appear.
 * @property emoteType the type of emote. The possible values are: `bitstier`, `follower`, `subscriptions`, `channelpoints`, `rewards`, `hypetrain`, `prime`, `turbo`, `smilies`, `globals`, `owl2019`, `twofactor`, `limitedtime`.
 * @property emoteSetId an ID that identifies the emote set that the emote belongs to.
 * @property ownerId the User ID of broadcaster who owns the emote.
 * @property format the formats that the emote is available in. For example, if the emote is available only as a static PNG, the array contains only `static`. But if the emote is available as a static PNG and an animated GIF, the array contains `static` and `animated`.
 * @property scale the sizes that the emote is available in. For example, if the emote is available in small and medium sizes, the array contains `1.0` and `2.0`.
 * @property themeMode the background themes that the emote is available in. Possible values are `dark` and `light`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-user-emotes">Twitch API Reference - Get User Emotes</a>
 */
@Serializable
data class UserEmote(
    val id: String,
    val name: String,
    @SerialName("emote_type") val emoteType: String,
    @SerialName("emote_set_id") val emoteSetId: String,
    @SerialName("owner_id") val ownerId: String,
    val format: List<String> = emptyList(),
    val scale: List<String> = emptyList(),
    @SerialName("theme_mode") val themeMode: List<String> = emptyList(),
)
