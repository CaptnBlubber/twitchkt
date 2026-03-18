package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a channel subscription from the Twitch Helix API.
 *
 * @property broadcasterId an ID that identifies the broadcaster.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property gifterId the ID of the user that gifted the subscription to the user. Is an empty
 * string if [isGift] is `false`.
 * @property gifterLogin the gifter's login name. Is an empty string if [isGift] is `false`.
 * @property gifterName the gifter's display name. Is an empty string if [isGift] is `false`.
 * @property isGift a Boolean value that determines whether the subscription is a gift
 * subscription. Is `true` if the subscription was gifted.
 * @property planName the name of the subscription.
 * @property tier the type of subscription. Possible values are: `1000` (Tier 1), `2000`
 * (Tier 2), `3000` (Tier 3).
 * @property userId an ID that identifies the subscribing user.
 * @property userName the user's display name.
 * @property userLogin the user's login name.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-broadcaster-subscriptions">Twitch API Reference - Get Broadcaster Subscriptions</a>
 */
@Serializable
data class Subscription(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("gifter_id") val gifterId: String = "",
    @SerialName("gifter_login") val gifterLogin: String = "",
    @SerialName("gifter_name") val gifterName: String = "",
    @SerialName("is_gift") val isGift: Boolean = false,
    val tier: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String = "",
    @SerialName("user_login") val userLogin: String = "",
)
