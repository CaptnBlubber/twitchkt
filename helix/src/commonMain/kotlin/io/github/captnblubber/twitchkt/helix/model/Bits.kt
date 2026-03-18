package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents an entry on the Bits leaderboard.
 *
 * @property userId an ID that identifies a user on the leaderboard.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property rank the user's position on the leaderboard.
 * @property score the number of Bits the user has cheered.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-bits-leaderboard">Twitch API Reference - Get Bits Leaderboard</a>
 */
@Serializable
data class BitsLeaderEntry(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val rank: Int,
    val score: Int,
)

/**
 * Represents a Cheermote that users can use to cheer Bits.
 *
 * @property prefix the name portion of the Cheermote string that you use in chat to cheer Bits.
 * The full Cheermote string is the concatenation of {prefix} + {number of Bits}. For example,
 * if the prefix is "Cheer" and you want to cheer 100 Bits, the full Cheermote string is Cheer100.
 * @property tiers a list of tier levels that the Cheermote supports. Each tier identifies the range
 * of Bits that you can cheer at that tier level and an image that graphically identifies the tier level.
 * @property type the type of Cheermote. Possible values are: `global_first_party`, `global_third_party`,
 * `channel_custom`, `display_only`, `sponsored`.
 * @property order the order that the Cheermotes are shown in the Bits card.
 * @property lastUpdated the date and time, in RFC3339 format, when this Cheermote was last updated.
 * @property isCharitable a Boolean value that indicates whether this Cheermote provides a charitable
 * contribution match during charity campaigns.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-cheermotes">Twitch API Reference - Get Cheermotes</a>
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class Cheermote(
    val prefix: String,
    val tiers: List<CheermoteTier>,
    val type: String,
    val order: Int,
    @SerialName("last_updated") val lastUpdated: Instant,
    @SerialName("is_charitable") val isCharitable: Boolean,
)

/**
 * Represents a tier level of a Cheermote.
 *
 * @property minBits the minimum number of Bits that you must cheer at this tier level.
 * @property id the tier level. Possible tiers are: `1`, `100`, `500`, `1000`, `5000`, `10000`, `100000`.
 * @property color the hex code of the color associated with this tier level (for example, #979797).
 * @property images the animated and static image sets for the Cheermote. The dictionary of images
 * is organized by theme, format, and size. The theme keys are `dark` and `light`. Each theme is a
 * dictionary of formats: `animated` and `static`. Each format is a dictionary of sizes: `1`, `1.5`,
 * `2`, `3`, and `4`. The value of each size contains the URL to the image.
 * @property canCheer a Boolean value that determines whether users can cheer at this tier level.
 * @property showInBitsCard a Boolean value that determines whether this tier level is shown in the Bits card.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-cheermotes">Twitch API Reference - Get Cheermotes</a>
 */
@Serializable
data class CheermoteTier(
    @SerialName("min_bits") val minBits: Int,
    val id: String,
    val color: String,
    val images: JsonObject,
    @SerialName("can_cheer") val canCheer: Boolean,
    @SerialName("show_in_bits_card") val showInBitsCard: Boolean,
)
