package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.vip.add](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelvipadd)
 *
 * @property userId the user ID of the new VIP.
 * @property userLogin the login of the new VIP.
 * @property userName the display name of the new VIP.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 */
data class ChannelVipAdd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
) : TwitchEvent
