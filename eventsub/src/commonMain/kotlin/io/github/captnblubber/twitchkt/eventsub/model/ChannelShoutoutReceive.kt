package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.shoutout.receive](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshoutoutreceive)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property fromBroadcasterUserId the user ID of the broadcaster who sent the shoutout.
 * @property fromBroadcasterUserLogin the login of the broadcaster who sent the shoutout.
 * @property fromBroadcasterUserName the display name of the broadcaster who sent the shoutout.
 * @property viewerCount the number of viewers at the time of the shoutout.
 * @property startedAt RFC3339 timestamp of when the shoutout was received.
 */
data class ChannelShoutoutReceive(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val fromBroadcasterUserId: String,
    val fromBroadcasterUserLogin: String,
    val fromBroadcasterUserName: String,
    val viewerCount: Int,
    val startedAt: Instant,
) : TwitchEvent
