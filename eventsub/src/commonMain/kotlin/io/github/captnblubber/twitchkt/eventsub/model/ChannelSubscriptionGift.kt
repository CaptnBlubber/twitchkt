package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.subscription.gift](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscriptiongift)
 *
 * @property userId the user ID of the user who sent the gift subscription. Null if anonymous.
 * @property userLogin the user login of the user who sent the gift subscription. Null if anonymous.
 * @property userName the user display name of the user who sent the gift subscription. Null if anonymous.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property total the number of subscriptions in the gift.
 * @property tier the tier of the subscription gift: `1000` (Tier 1), `2000` (Tier 2), or `3000` (Tier 3).
 * @property cumulativeTotal the number of gift subscriptions the gifter has given in this channel. Null if anonymous.
 * @property isAnonymous whether the gift was anonymous.
 */
data class ChannelSubscriptionGift(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String?,
    val userLogin: String?,
    val userName: String?,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val total: Int,
    val tier: String,
    val cumulativeTotal: Int?,
    val isAnonymous: Boolean,
) : TwitchEvent
