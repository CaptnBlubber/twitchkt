package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.warning.send](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelwarningsend)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who sent the warning.
 * @property moderatorUserLogin the login of the moderator who sent the warning.
 * @property moderatorUserName the display name of the moderator who sent the warning.
 * @property userId the user ID of the warned user.
 * @property userLogin the login of the warned user.
 * @property userName the display name of the warned user.
 * @property reason the reason given for the warning.
 * @property chatRulesCited the list of chat rules cited in the warning.
 */
data class ChannelWarningSend(
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
    val reason: String?,
    val chatRulesCited: List<String>?,
) : TwitchEvent
