package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a creator goal from the Twitch Helix API.
 *
 * @property id an ID that identifies this goal.
 * @property broadcasterId an ID that identifies the broadcaster that created the goal.
 * @property broadcasterName the broadcaster's display name.
 * @property broadcasterLogin the broadcaster's login name.
 * @property type the type of goal. Possible values are: `follower`, `subscription`,
 * `subscription_count`, `new_subscription`, `new_subscription_count`.
 * @property description a description of the goal. Is an empty string if not specified.
 * @property currentAmount the goal's current value. The goal's [type] determines how this
 * value is increased or decreased.
 * @property targetAmount the goal's target value. For example, if the broadcaster has 200
 * followers before creating the goal, and their goal is to double that number, this field
 * is set to 400.
 * @property createdAt the UTC date and time that the broadcaster created the goal.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-creator-goals">Twitch API Reference - Get Creator Goals</a>
 */
@Serializable
data class CreatorGoal(
    val id: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    val type: String,
    val description: String,
    @SerialName("current_amount") val currentAmount: Int,
    @SerialName("target_amount") val targetAmount: Int,
    @SerialName("created_at") val createdAt: Instant,
)
