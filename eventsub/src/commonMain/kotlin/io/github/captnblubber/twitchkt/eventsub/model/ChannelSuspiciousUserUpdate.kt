package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.suspicious_user.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsuspicious_userupdate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who updated the suspicious user status.
 * @property moderatorUserLogin the login of the moderator.
 * @property moderatorUserName the display name of the moderator.
 * @property userId the user ID of the suspicious user.
 * @property userLogin the login of the suspicious user.
 * @property userName the display name of the suspicious user.
 * @property lowTrustStatus the updated low trust status.
 */
data class ChannelSuspiciousUserUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val lowTrustStatus: String,
) : TwitchEvent
