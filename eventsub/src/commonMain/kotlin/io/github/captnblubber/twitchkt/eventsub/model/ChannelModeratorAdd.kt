package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.moderator.add](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderatoradd)
 *
 * @property userId the user ID of the new moderator.
 * @property userLogin the login of the new moderator.
 * @property userName the display name of the new moderator.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 */
data class ChannelModeratorAdd(
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
