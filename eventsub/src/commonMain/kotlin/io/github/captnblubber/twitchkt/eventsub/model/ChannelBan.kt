package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.ban](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelban)
 *
 * @property userId the user ID of the user who was banned.
 * @property userLogin the login of the user who was banned.
 * @property userName the display name of the user who was banned.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who issued the ban.
 * @property moderatorUserLogin the login of the moderator who issued the ban.
 * @property moderatorUserName the display name of the moderator who issued the ban.
 * @property reason the reason given for the ban.
 * @property bannedAt RFC3339 timestamp of when the ban was issued.
 * @property endsAt RFC3339 timestamp of when the timeout ends; `null` for permanent bans.
 * @property isPermanent whether the ban is permanent.
 */
data class ChannelBan(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val reason: String,
    val bannedAt: Instant,
    val endsAt: Instant?,
    val isPermanent: Boolean,
) : TwitchEvent
