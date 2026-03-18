package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.user_message_update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatuser_message_update)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the user whose message was updated.
 * @property userLogin the login of the user whose message was updated.
 * @property userName the display name of the user whose message was updated.
 * @property status the new status of the message (e.g. `approved`, `denied`).
 * @property chatMessageId the ID of the updated message.
 * @property message the updated message content.
 */
data class ChannelChatUserMessageUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val status: String,
    val chatMessageId: String,
    val message: ChatMessage,
) : TwitchEvent
