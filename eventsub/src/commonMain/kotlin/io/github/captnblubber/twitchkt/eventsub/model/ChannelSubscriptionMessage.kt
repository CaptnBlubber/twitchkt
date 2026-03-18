package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.subscription.message](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscriptionmessage)
 *
 * @property userId the user ID of the user who sent a resubscription chat message.
 * @property userLogin the user login of the user who sent a resubscription chat message.
 * @property userName the user display name of the user who sent a resubscription chat message.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property tier the tier of the user's subscription: `1000` (Tier 1), `2000` (Tier 2), or `3000` (Tier 3).
 * @property message an object containing the resubscription message text and emote information.
 * @property cumulativeMonths the total number of months the user has been subscribed to the channel.
 * @property streakMonths the number of consecutive months the user's current subscription has been active. Null if opted out.
 * @property durationMonths the month duration of the subscription.
 */
data class ChannelSubscriptionMessage(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val tier: String,
    val message: ChatMessage,
    val cumulativeMonths: Int,
    val streakMonths: Int?,
    val durationMonths: Int,
) : TwitchEvent
