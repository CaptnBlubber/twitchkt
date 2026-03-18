package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.shield_mode.begin](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshield_modebegin)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who activated shield mode.
 * @property moderatorUserLogin the login of the moderator who activated shield mode.
 * @property moderatorUserName the display name of the moderator who activated shield mode.
 * @property startedAt RFC3339 timestamp of when shield mode was activated.
 */
data class ChannelShieldModeBegin(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val startedAt: Instant,
) : TwitchEvent
