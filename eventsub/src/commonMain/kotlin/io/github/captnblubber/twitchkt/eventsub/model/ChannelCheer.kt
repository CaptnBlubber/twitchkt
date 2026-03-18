package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.cheer](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcheer)
 *
 * @property isAnonymous whether the cheer was anonymous.
 * @property userId the user ID of the user who cheered; `null` if anonymous.
 * @property userLogin the login of the user who cheered; `null` if anonymous.
 * @property userName the display name of the user who cheered; `null` if anonymous.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property message the chat message sent with the cheer.
 * @property bits the number of Bits cheered.
 */
data class ChannelCheer(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val isAnonymous: Boolean,
    val userId: String?,
    val userLogin: String?,
    val userName: String?,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val message: String,
    val bits: Int,
) : TwitchEvent
