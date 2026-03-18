package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.unban_request.resolve](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban_requestresolve)
 *
 * @property id the ID of the unban request.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who resolved the request.
 * @property moderatorUserLogin the login of the moderator who resolved the request.
 * @property moderatorUserName the display name of the moderator who resolved the request.
 * @property userId the user ID of the user whose unban request was resolved.
 * @property userLogin the login of the user whose unban request was resolved.
 * @property userName the display name of the user whose unban request was resolved.
 * @property resolutionText the resolution text provided by the moderator.
 * @property status the resolution status: `approved` or `denied`.
 */
data class ChannelUnbanRequestResolve(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val resolutionText: String?,
    val status: String,
) : TwitchEvent
