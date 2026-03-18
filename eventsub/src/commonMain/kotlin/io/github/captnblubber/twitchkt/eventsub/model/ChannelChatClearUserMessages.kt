package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.clear_user_messages](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatclear_user_messages)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property targetUserId the user ID of the user whose messages were cleared.
 * @property targetUserLogin the login of the user whose messages were cleared.
 * @property targetUserName the display name of the user whose messages were cleared.
 */
data class ChannelChatClearUserMessages(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val targetUserId: String,
    val targetUserLogin: String,
    val targetUserName: String,
) : TwitchEvent
