package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.user_message_hold](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatuser_message_hold)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the user whose message was held.
 * @property userLogin the login of the user whose message was held.
 * @property userName the display name of the user whose message was held.
 * @property chatMessageId the ID of the held message.
 * @property message the held message content.
 */
data class ChannelChatUserMessageHold(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val chatMessageId: String,
    val message: ChatMessage,
) : TwitchEvent
