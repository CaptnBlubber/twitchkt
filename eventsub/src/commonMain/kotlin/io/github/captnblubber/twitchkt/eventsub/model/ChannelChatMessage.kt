package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.message](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatmessage)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property chatterUserId the user ID of the user that sent the message.
 * @property chatterUserLogin the user login of the user that sent the message.
 * @property chatterUserName the user display name of the user that sent the message.
 * @property chatMessageId a UUID that identifies the message.
 * @property message the structured chat message, containing text and fragments.
 * @property color the color of the user's name in the chat room. Empty string if not set.
 * @property badges list of chat badges worn by the chatter.
 * @property messageType the type of message.
 * @property cheer cheer metadata if the message includes a cheer; `null` otherwise.
 * @property reply reply metadata if this message is a reply to another message; `null` otherwise.
 * @property channelPointsCustomRewardId the ID of a channel points custom reward that was redeemed; `null` if not a reward redemption.
 */
data class ChannelChatMessage(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val chatterUserId: String,
    val chatterUserLogin: String,
    val chatterUserName: String,
    val chatMessageId: String,
    val message: ChatMessage,
    val color: String,
    val badges: List<ChatBadge>,
    val messageType: ChatMessageType,
    val cheer: ChatCheer?,
    val reply: ChatReply?,
    val channelPointsCustomRewardId: String?,
) : TwitchEvent
