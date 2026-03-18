package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a redemption of a custom channel point reward.
 *
 * @property broadcasterId the ID that uniquely identifies the broadcaster.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property id the ID that uniquely identifies this redemption.
 * @property userId the ID that uniquely identifies the user that redeemed the reward.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property userInput the text the user entered at the prompt when they redeemed the reward;
 * otherwise, an empty string if user input was not required.
 * @property status the state of the redemption. Possible values are: `CANCELED`, `FULFILLED`, `UNFULFILLED`.
 * @property redeemedAt the date and time of when the reward was redeemed, in RFC3339 format.
 * @property reward the reward that the user redeemed.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-custom-reward-redemption">Twitch API Reference - Get Custom Reward Redemption</a>
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class RewardRedemption(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("user_input") val userInput: String = "",
    val status: String,
    @SerialName("redeemed_at") val redeemedAt: Instant,
    val reward: RedeemedReward,
)

/**
 * Summary of the reward that was redeemed, as returned in a [RewardRedemption].
 *
 * @property id the ID that uniquely identifies the redeemed reward.
 * @property title the reward's title.
 * @property prompt the prompt displayed to the viewer if user input is required.
 * @property cost the reward's cost, in Channel Points.
 */
@Serializable
data class RedeemedReward(
    val id: String,
    val title: String,
    val prompt: String = "",
    val cost: Int,
)
