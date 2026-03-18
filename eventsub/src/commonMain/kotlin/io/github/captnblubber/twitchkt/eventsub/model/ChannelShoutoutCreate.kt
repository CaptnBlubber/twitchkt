package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.shoutout.create](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshoutoutcreate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who created the shoutout.
 * @property moderatorUserLogin the login of the moderator who created the shoutout.
 * @property moderatorUserName the display name of the moderator who created the shoutout.
 * @property toBroadcasterUserId the user ID of the broadcaster receiving the shoutout.
 * @property toBroadcasterUserLogin the login of the broadcaster receiving the shoutout.
 * @property toBroadcasterUserName the display name of the broadcaster receiving the shoutout.
 * @property viewerCount the number of viewers at the time of the shoutout.
 * @property startedAt RFC3339 timestamp of when the shoutout started.
 * @property cooldownEndsAt RFC3339 timestamp of when the shoutout cooldown ends.
 * @property targetCooldownEndsAt RFC3339 timestamp of when the target cooldown ends.
 */
data class ChannelShoutoutCreate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val toBroadcasterUserId: String,
    val toBroadcasterUserLogin: String,
    val toBroadcasterUserName: String,
    val viewerCount: Int,
    val startedAt: Instant,
    val cooldownEndsAt: Instant,
    val targetCooldownEndsAt: Instant,
) : TwitchEvent
