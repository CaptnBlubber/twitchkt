package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.shield_mode.end](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshield_modeend)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who deactivated shield mode.
 * @property moderatorUserLogin the login of the moderator who deactivated shield mode.
 * @property moderatorUserName the display name of the moderator who deactivated shield mode.
 * @property endedAt RFC3339 timestamp of when shield mode was deactivated.
 */
data class ChannelShieldModeEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val endedAt: Instant,
) : TwitchEvent
