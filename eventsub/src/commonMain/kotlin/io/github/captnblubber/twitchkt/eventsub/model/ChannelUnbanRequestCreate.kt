package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.unban_request.create](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban_requestcreate)
 *
 * @property id the ID of the unban request.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the user requesting to be unbanned.
 * @property userLogin the login of the user requesting to be unbanned.
 * @property userName the display name of the user requesting to be unbanned.
 * @property text the text of the unban request.
 * @property createdAt RFC3339 timestamp of when the request was created.
 */
data class ChannelUnbanRequestCreate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val text: String,
    val createdAt: Instant,
) : TwitchEvent
