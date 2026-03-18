package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.raid](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelraid)
 *
 * @property fromBroadcasterUserId the broadcaster ID that created the raid.
 * @property fromBroadcasterUserLogin the broadcaster login that created the raid.
 * @property fromBroadcasterUserName the broadcaster display name that created the raid.
 * @property toBroadcasterUserId the broadcaster ID that received the raid.
 * @property toBroadcasterUserLogin the broadcaster login that received the raid.
 * @property toBroadcasterUserName the broadcaster display name that received the raid.
 * @property viewers the number of viewers in the raid.
 */
data class ChannelRaid(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val fromBroadcasterUserId: String,
    val fromBroadcasterUserLogin: String,
    val fromBroadcasterUserName: String,
    val toBroadcasterUserId: String,
    val toBroadcasterUserLogin: String,
    val toBroadcasterUserName: String,
    val viewers: Int,
) : TwitchEvent
