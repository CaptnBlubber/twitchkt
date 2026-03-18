package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.follow](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelfollow)
 *
 * @property userId the user ID of the user now following the specified channel.
 * @property userLogin the user login of the user now following the specified channel.
 * @property userName the user display name of the user now following the specified channel.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property followedAt RFC3339 timestamp of when the follow occurred.
 */
data class ChannelFollow(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val followedAt: Instant,
) : TwitchEvent
