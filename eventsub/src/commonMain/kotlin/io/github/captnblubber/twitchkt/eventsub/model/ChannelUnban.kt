package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.unban](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban)
 *
 * @property userId the user ID of the user who was unbanned.
 * @property userLogin the login of the user who was unbanned.
 * @property userName the display name of the user who was unbanned.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who lifted the ban.
 * @property moderatorUserLogin the login of the moderator who lifted the ban.
 * @property moderatorUserName the display name of the moderator who lifted the ban.
 */
data class ChannelUnban(
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
) : TwitchEvent
